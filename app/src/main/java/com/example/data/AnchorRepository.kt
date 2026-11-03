package com.example.data

import android.content.Context
import android.util.Log
import com.example.ai.AiValidator
import com.example.ai.GeminiService
import com.example.ai.ParsedCapture
import com.example.notification.AnchorAlarmScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AnchorRepository(
    private val dao: AnchorDao,
    private val geminiService: GeminiService,
    private val appContext: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val TAG = "AnchorRepository"

    suspend fun findPotentialDuplicate(rawText: String): AnchorItem? {
        val parsed = geminiService.parseWithLocalHeuristics(rawText).firstOrNull() ?: return null
        val stopWords = setOf("a", "an", "the", "to", "for", "me", "my", "please", "today", "tomorrow", "next", "this", "on", "by", "at")
        fun tokens(value: String): Set<String> = value.lowercase(Locale.ROOT)
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length > 1 && it !in stopWords }
            .toSet()
        fun similarity(left: Set<String>, right: Set<String>): Double {
            if (left.isEmpty() || right.isEmpty()) return 0.0
            if (left == right) return 1.0
            return left.intersect(right).size.toDouble() / left.union(right).size
        }

        val target = tokens(parsed.title)
        if (target.isEmpty()) return null
        val active = dao.getAllActiveItemsSync().asSequence()
            .filter { it.type == parsed.type && it.parentId == null }
            .take(200)
            .toList()
        return active.mapNotNull { item ->
            val titleScore = similarity(target, tokens(item.title))
            val rawTitle = item.rawInput.takeIf { it.isNotBlank() }
                ?.let { geminiService.parseWithLocalHeuristics(it).firstOrNull()?.title }
            val rawScore = rawTitle?.let { similarity(target, tokens(it)) } ?: 0.0
            item to maxOf(titleScore, rawScore)
        }.filter { (item, score) ->
            val existingTokens = tokens(item.title)
            score >= 0.78 || (target.size == 1 && existingTokens == target)
        }.maxByOrNull { it.second }?.first
    }

    val allInbox: Flow<List<AnchorItem>> = dao.getInboxItems()
    val allToday: Flow<List<AnchorItem>> = dao.getTodayItems()
    val allNotes: Flow<List<AnchorItem>> = dao.getNotes()
    val allDone: Flow<List<AnchorItem>> = dao.getDoneItems()
    val habitualSnoozed: Flow<List<AnchorItem>> = dao.getHabitualSnoozedItems(3)

    fun getUpcoming(todayIso: String): Flow<List<AnchorItem>> {
        return dao.getUpcomingItems(todayIso)
    }

    fun getSubtasks(parentId: Long): Flow<List<AnchorItem>> {
        return dao.getSubtasks(parentId)
    }

    /**
     * Zero-friction instant capture:
     * 1. Immediately saves initial item to Room DB so UI updates instantly.
     * 2. Asynchronously enriches via AI in background.
     * 3. Once AI finishes, updates the record atomically and automatically schedules reminders if a deadline exists.
     */
    suspend fun captureInstant(
        rawText: String,
        sharedUri: String? = null,
        onComplete: ((AnchorItem) -> Unit)? = null
    ): Long {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty() && sharedUri == null) return -1L

        val retainedSharedImage = sharedUri?.let { source ->
            withContext(Dispatchers.IO) { SharedAttachmentStore.retain(appContext, source) }
        }
        val initialTitle = if (trimmed.isNotEmpty()) trimmed else if (retainedSharedImage != null) "Shared image" else "Shared image unavailable"
        val placeholder = AnchorItem(
            title = initialTitle,
            rawInput = trimmed,
            type = if (trimmed.startsWith("note:", ignoreCase = true)) AnchorItem.TYPE_NOTE else AnchorItem.TYPE_TASK,
            status = AnchorItem.STATUS_INBOX,
            aiSorted = false,
            sharedUri = retainedSharedImage
        )

        val initialId = dao.insert(placeholder)

        // Asynchronous AI enrichment in background
        externalScope.launch {
            try {
                val parsedList = geminiService.parseCapture(trimmed)
                if (parsedList.isNotEmpty()) {
                    // Check latest DB state to avoid clobbering user edits or completed items
                    val currentItem = dao.getItemById(initialId)
                    if (currentItem != null && currentItem.status != AnchorItem.STATUS_DONE && !currentItem.aiSorted) {
                        val firstParsed = AiValidator.normalize(parsedList[0], trimmed)

                        // The SQL guard is the final arbiter: a user edit that lands between
                        // this read and the update must win over the slower model response.
                        val updatedRows = dao.updateAiEnrichment(
                            id = initialId,
                            type = firstParsed.type,
                            title = firstParsed.title,
                            dueDate = firstParsed.dueDate,
                            dueTime = firstParsed.dueTime,
                            priority = firstParsed.priority,
                            tags = firstParsed.tags.joinToString(","),
                            estimatedMinutes = firstParsed.estimatedMinutes
                        )

                        val updatedItem = if (updatedRows > 0) dao.getItemById(initialId) else null
                        if (updatedItem != null) {
                            if (updatedItem.type == AnchorItem.TYPE_TASK && updatedItem.dueDate != null && updatedItem.status != AnchorItem.STATUS_DONE) {
                                AnchorAlarmScheduler.scheduleTaskReminder(appContext, updatedItem)
                            }
                            onComplete?.invoke(updatedItem)
                        }

                        // If multiple distinct tasks were detected in the single capture
                        if (updatedRows > 0 && parsedList.size > 1) {
                            for (i in 1 until parsedList.size) {
                                val extra = AiValidator.normalize(parsedList[i], trimmed)
                                val extraItem = AnchorItem(
                                    title = extra.title,
                                    rawInput = trimmed,
                                    type = extra.type,
                                    dueDate = extra.dueDate,
                                    dueTime = extra.dueTime,
                                    priority = extra.priority,
                                    tags = extra.tags.joinToString(","),
                                    estimatedMinutes = extra.estimatedMinutes,
                                    status = AnchorItem.STATUS_INBOX,
                                    aiSorted = true
                                )
                                val extraId = dao.insert(extraItem)
                                if (extra.dueDate != null && extra.type == AnchorItem.TYPE_TASK) {
                                    val insertedExtra = dao.getItemById(extraId)
                                    if (insertedExtra != null) {
                                        AnchorAlarmScheduler.scheduleTaskReminder(appContext, insertedExtra)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Background AI enrichment error (${e.javaClass.simpleName})")
            }
        }

        return initialId
    }

    suspend fun moveToToday(item: AnchorItem) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val updated = item.copy(
            status = AnchorItem.STATUS_TODAY,
            dueDate = item.dueDate ?: todayStr,
            isPinnedToToday = true,
            aiSorted = true
        )
        dao.update(updated)
        if (updated.dueDate != null) {
            AnchorAlarmScheduler.scheduleTaskReminder(appContext, updated)
        }
    }

    suspend fun moveToInbox(item: AnchorItem) {
        dao.update(
            item.copy(
                status = AnchorItem.STATUS_INBOX,
                isPinnedToToday = false
            )
        )
    }

    suspend fun markCompleted(item: AnchorItem) {
        AnchorAlarmScheduler.cancelTaskReminder(appContext, item.id)
        dao.update(
            item.copy(
                status = AnchorItem.STATUS_DONE,
                completedAt = System.currentTimeMillis(),
                aiSorted = true
            )
        )
        // Auto-reschedule recurring commitments
        if (!item.recurrenceRule.isNullOrBlank()) {
            val nextDate = calculateNextRecurrenceDate(item.dueDate, item.recurrenceRule)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val nextItem = item.copy(
                id = 0L,
                status = if (nextDate == todayStr) AnchorItem.STATUS_TODAY else AnchorItem.STATUS_INBOX,
                dueDate = nextDate,
                completedAt = null,
                snoozeCount = 0,
                snoozeUntil = null,
                createdAt = System.currentTimeMillis()
            )
            val newId = dao.insert(nextItem)
            val inserted = dao.getItemById(newId)
            if (inserted != null && inserted.dueDate != null) {
                AnchorAlarmScheduler.scheduleTaskReminder(appContext, inserted)
            }
        }
    }

    suspend fun uncomplete(item: AnchorItem) {
        val updated = item.copy(
            status = AnchorItem.STATUS_INBOX,
            completedAt = null,
            aiSorted = true
        )
        dao.update(updated)
        if (updated.dueDate != null) {
            AnchorAlarmScheduler.scheduleTaskReminder(appContext, updated)
        }
    }

    suspend fun snooze(item: AnchorItem, hours: Int = 0, minutes: Int = 0) {
        val cal = Calendar.getInstance()
        if (hours > 0) cal.add(Calendar.HOUR_OF_DAY, hours)
        if (minutes > 0) cal.add(Calendar.MINUTE, minutes)
        if (hours == 0 && minutes == 0) cal.add(Calendar.HOUR_OF_DAY, 24)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val snoozeDate = dateFormat.format(cal.time)
        val todayDate = dateFormat.format(Date())

        val newSnoozeCount = item.snoozeCount + 1

        val snoozed = item.copy(
            snoozeCount = newSnoozeCount,
            snoozeUntil = cal.timeInMillis,
            aiSorted = true,
            dueDate = snoozeDate,
            dueTime = timeFormat.format(cal.time),
            status = if (item.status == AnchorItem.STATUS_TODAY && snoozeDate == todayDate) {
                AnchorItem.STATUS_TODAY
            } else {
                AnchorItem.STATUS_INBOX
            },
            isPinnedToToday = item.status == AnchorItem.STATUS_TODAY && snoozeDate == todayDate
        )

        dao.update(snoozed)
        AnchorAlarmScheduler.scheduleTaskReminder(appContext, snoozed)
    }

    suspend fun delete(item: AnchorItem) {
        AnchorAlarmScheduler.cancelTaskReminder(appContext, item.id)
        dao.deleteSubtasksByParentId(item.id)
        dao.delete(item)
        val attachmentPath = item.sharedUri ?: return
        externalScope.launch {
            delay(10_000L) // Leave time for the in-app undo action to restore the item.
            val stillReferenced = dao.getAllItems().first().any { it.sharedUri == attachmentPath }
            if (!stillReferenced) SharedAttachmentStore.delete(appContext, attachmentPath)
        }
    }

    suspend fun updateItem(item: AnchorItem) {
        // Editing is an explicit user decision; never let a pending AI parse replace it.
        val userEditedItem = item.copy(aiSorted = true)
        dao.update(userEditedItem)
        if (userEditedItem.status != AnchorItem.STATUS_DONE && userEditedItem.dueDate != null) {
            AnchorAlarmScheduler.scheduleTaskReminder(appContext, userEditedItem)
        } else {
            AnchorAlarmScheduler.cancelTaskReminder(appContext, userEditedItem.id)
        }
    }

    suspend fun getItem(id: Long): AnchorItem? {
        return dao.getItemById(id)
    }

    suspend fun getOverdueItems(): List<AnchorItem> {
        val now = Date()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        return dao.getOverdueItems(todayStr, timeStr)
    }

    suspend fun getActiveTasksSync(): List<AnchorItem> {
        return dao.getAllActiveTasksSync()
    }

    suspend fun getAllActiveItemsSync(): List<AnchorItem> {
        return dao.getAllActiveItemsSync()
    }

    suspend fun updateSubtask(item: AnchorItem, stepIndex: Int, completed: Boolean): AnchorItem {
        val steps = item.aiBreakdownSteps?.split("\n")?.toMutableList() ?: mutableListOf()
        if (stepIndex in steps.indices) {
            val original = steps[stepIndex].replace(Regex("^\\[[ xX]\\]\\s*"), "")
            steps[stepIndex] = if (completed) "[x] $original" else "[ ] $original"
            val updated = item.copy(aiBreakdownSteps = steps.joinToString("\n"))
            dao.update(updated)
            return updated
        }
        return item
    }

    suspend fun addSubtask(item: AnchorItem, stepText: String): AnchorItem {
        val trimmed = stepText.trim()
        if (trimmed.isEmpty()) return item
        val steps = item.aiBreakdownSteps?.split("\n")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
        steps.add("[ ] $trimmed")
        val updated = item.copy(aiBreakdownSteps = steps.joinToString("\n"))
        dao.update(updated)

        // Also persist as child AnchorItem
        dao.insert(
            AnchorItem(
                parentId = item.id,
                title = trimmed,
                rawInput = "Micro-step for: ${item.title}",
                type = AnchorItem.TYPE_TASK,
                status = item.status,
                dueDate = item.dueDate,
                priority = item.priority,
                tags = item.tags,
                estimatedMinutes = 10,
                aiSorted = true
            )
        )
        return updated
    }

    /**
     * Safe task breakdown:
     * - Preserves parent task context and identity (never deletes original task).
     * - Clears previous subtasks for this parent to prevent duplicates.
     * - Inserts child tasks linked via parentId.
     * - Updates parent task's aiBreakdownSteps.
     */
    suspend fun applyBreakdownSubtasks(parentTask: AnchorItem, subtasks: List<String>): AnchorItem {
        val validatedSubtasks = AiValidator.validateSubtasks(subtasks)
        if (validatedSubtasks.isEmpty()) return parentTask

        // Delete any existing subtasks for this parent to avoid duplicates
        dao.deleteSubtasksByParentId(parentTask.id)

        // Insert new subtask children
        val subtaskEntities = validatedSubtasks.mapIndexed { index, stepTitle ->
            AnchorItem(
                parentId = parentTask.id,
                title = stepTitle,
                rawInput = "Micro-step ${index + 1} of: ${parentTask.title}",
                type = AnchorItem.TYPE_TASK,
                status = parentTask.status,
                dueDate = parentTask.dueDate,
                priority = parentTask.priority,
                tags = parentTask.tags,
                estimatedMinutes = 10,
                aiSorted = true
            )
        }
        dao.insertAll(subtaskEntities)

        // Update parent with checklist
        val checklist = validatedSubtasks.joinToString("\n") { "[ ] $it" }
        val updatedParent = parentTask.copy(aiBreakdownSteps = checklist)
        dao.update(updatedParent)
        return updatedParent
    }

    fun searchAll(query: String): Flow<List<AnchorItem>> = dao.searchAllItems(query)

    fun calculateNextRecurrenceDate(currentDueDate: String?, recurrenceRule: String?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        if (!currentDueDate.isNullOrBlank()) {
            try {
                val parsed = sdf.parse(currentDueDate)
                if (parsed != null) cal.time = parsed
            } catch (_: Exception) {}
        }
        val todayCal = Calendar.getInstance()
        if (cal.before(todayCal)) {
            cal.time = todayCal.time
        }

        when (recurrenceRule) {
            AnchorItem.RECURRENCE_DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            AnchorItem.RECURRENCE_WEEKDAYS -> {
                do {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                } while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            }
            AnchorItem.RECURRENCE_WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            AnchorItem.RECURRENCE_MONTHLY -> cal.add(Calendar.MONTH, 1)
            else -> cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return sdf.format(cal.time)
    }

    suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val active = dao.getAllActiveItemsSync()
        val array = org.json.JSONArray()
        for (item in active) {
            val obj = org.json.JSONObject().apply {
                put("title", item.title)
                put("rawInput", item.rawInput)
                put("type", item.type)
                put("dueDate", item.dueDate ?: org.json.JSONObject.NULL)
                put("dueTime", item.dueTime ?: org.json.JSONObject.NULL)
                put("priority", item.priority)
                put("tags", item.tags)
                put("estimatedMinutes", item.estimatedMinutes ?: org.json.JSONObject.NULL)
                put("status", item.status)
                put("isPinnedToToday", item.isPinnedToToday)
                put("todayOrder", item.todayOrder)
                put("createdAt", item.createdAt)
                put("completedAt", item.completedAt ?: org.json.JSONObject.NULL)
                put("recurrenceRule", item.recurrenceRule ?: org.json.JSONObject.NULL)
            }
            array.put(obj)
        }
        array.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Int = withContext(Dispatchers.IO) {
        try {
            val array = org.json.JSONArray(jsonStr)
            val itemsToInsert = mutableListOf<AnchorItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val item = AnchorItem(
                    title = obj.getString("title"),
                    rawInput = obj.optString("rawInput", ""),
                    type = obj.optString("type", AnchorItem.TYPE_TASK),
                    dueDate = if (obj.isNull("dueDate")) null else obj.getString("dueDate"),
                    dueTime = if (obj.isNull("dueTime")) null else obj.getString("dueTime"),
                    priority = obj.optString("priority", AnchorItem.PRIORITY_MEDIUM),
                    tags = obj.optString("tags", ""),
                    estimatedMinutes = if (obj.isNull("estimatedMinutes")) null else obj.getInt("estimatedMinutes"),
                    status = obj.optString("status", AnchorItem.STATUS_INBOX),
                    isPinnedToToday = obj.optBoolean("isPinnedToToday", false),
                    todayOrder = obj.optInt("todayOrder", 0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    completedAt = if (obj.isNull("completedAt")) null else obj.getLong("completedAt"),
                    recurrenceRule = if (obj.isNull("recurrenceRule")) null else obj.getString("recurrenceRule"),
                    aiSorted = true
                )
                itemsToInsert.add(item)
            }
            if (itemsToInsert.isNotEmpty()) {
                dao.insertAll(itemsToInsert)
            }
            itemsToInsert.size
        } catch (e: Exception) {
            Log.e(TAG, "Import error", e)
            0
        }
    }
}
