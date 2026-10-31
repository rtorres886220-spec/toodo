package com.example.ai

import com.example.data.AnchorItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Centralized, strict validation and normalization layer for all AI outputs.
 * Treats AI responses as untrusted input. Prevents corrupt data from entering the database.
 */
object AiValidator {

    private val DATE_REGEX = Regex("^20\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")
    private val TIME_REGEX = Regex("^([01]?\\d|2[0-3]):[0-5]\\d$")
    private val VALID_TYPES = setOf(AnchorItem.TYPE_TASK, AnchorItem.TYPE_NOTE)
    private val VALID_PRIORITIES = setOf(
        AnchorItem.PRIORITY_HIGH,
        AnchorItem.PRIORITY_MEDIUM,
        AnchorItem.PRIORITY_LOW,
        AnchorItem.PRIORITY_NONE
    )

    fun validateAndNormalizeType(rawType: String?): String {
        val lower = rawType?.trim()?.lowercase(Locale.getDefault()) ?: AnchorItem.TYPE_TASK
        return if (lower in VALID_TYPES) lower else AnchorItem.TYPE_TASK
    }

    fun validateAndNormalizeTitle(rawTitle: String?, fallback: String): String {
        val trimmed = rawTitle?.trim().orEmpty()
        val candidate = if (trimmed.isNotBlank()) trimmed else fallback.trim()
        return candidate.take(250).ifBlank { "Untitled Anchor" }
    }

    fun validateDate(rawDate: String?): String? {
        if (rawDate.isNullOrBlank()) return null
        val trimmed = rawDate.trim()
        if (!DATE_REGEX.matches(trimmed)) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
            }
            sdf.parse(trimmed)
            trimmed
        } catch (_: Exception) {
            null
        }
    }

    fun validateTime(rawTime: String?): String? {
        if (rawTime.isNullOrBlank()) return null
        val trimmed = rawTime.trim()
        if (!TIME_REGEX.matches(trimmed)) return null
        val parts = trimmed.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
        if (hour in 0..23 && minute in 0..59) {
            return String.format(Locale.ROOT, "%02d:%02d", hour, minute)
        }
        return null
    }

    fun validatePriority(rawPriority: String?): String {
        val lower = rawPriority?.trim()?.lowercase(Locale.getDefault()) ?: AnchorItem.PRIORITY_MEDIUM
        return if (lower in VALID_PRIORITIES) lower else AnchorItem.PRIORITY_MEDIUM
    }

    fun validateEstimatedMinutes(minutes: Int?): Int? {
        if (minutes == null) return null
        return if (minutes in 1..1440) minutes else null
    }

    fun validateTags(rawTags: List<String>?): List<String> {
        if (rawTags.isNullOrEmpty()) return emptyList()
        return rawTags
            .map { it.trim().lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9_-]"), "") }
            .filter { it.length in 2..24 }
            .distinct()
            .take(5)
    }

    fun validateSubtasks(subtasks: List<String>?): List<String> {
        if (subtasks.isNullOrEmpty()) return emptyList()
        return subtasks
            .map { it.trim().replace(Regex("^[-*•0-9.]+\\s*"), "") }
            .filter { it.isNotBlank() && it.length in 3..150 }
            .distinct()
            .take(8)
    }

    /**
     * Normalizes a parsed capture into a validated, safe structure.
     */
    fun normalize(capture: ParsedCapture, rawFallback: String): ParsedCapture {
        val type = validateAndNormalizeType(capture.type)
        return ParsedCapture(
            type = type,
            title = validateAndNormalizeTitle(capture.title, rawFallback),
            // Notes can mention dates without creating reminders.
            dueDate = if (type == AnchorItem.TYPE_TASK) validateDate(capture.dueDate) else null,
            dueTime = if (type == AnchorItem.TYPE_TASK) validateTime(capture.dueTime) else null,
            priority = validatePriority(capture.priority),
            tags = validateTags(capture.tags),
            estimatedMinutes = if (type == AnchorItem.TYPE_TASK) validateEstimatedMinutes(capture.estimatedMinutes) else null
        )
    }
}
