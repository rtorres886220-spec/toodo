package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "anchor_items",
    indices = [
        Index(value = ["parentId"]),
        Index(value = ["status"]),
        Index(value = ["dueDate"])
    ]
)
data class AnchorItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parentId: Long? = null,
    val title: String,
    val rawInput: String = "",
    val type: String = TYPE_TASK, // "task" | "note"
    val dueDate: String? = null,   // "YYYY-MM-DD"
    val dueTime: String? = null,   // "HH:mm"
    val priority: String = PRIORITY_MEDIUM, // "high" | "medium" | "low" | "none"
    val tags: String = "",         // comma-separated tags
    val estimatedMinutes: Int? = null,
    val status: String = STATUS_INBOX, // "INBOX" | "TODAY" | "DONE"
    val isPinnedToToday: Boolean = false,
    val todayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val snoozeCount: Int = 0,
    val snoozeUntil: Long? = null,
    val aiSorted: Boolean = false,
    val aiBreakdownSteps: String? = null, // JSON or bulleted micro-steps
    val sharedUri: String? = null
) {
    companion object {
        const val TYPE_TASK = "task"
        const val TYPE_NOTE = "note"

        const val STATUS_INBOX = "INBOX"
        const val STATUS_TODAY = "TODAY"
        const val STATUS_DONE = "DONE"

        const val PRIORITY_HIGH = "high"
        const val PRIORITY_MEDIUM = "medium"
        const val PRIORITY_LOW = "low"
        const val PRIORITY_NONE = "none"
    }

    val tagList: List<String>
        get() = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
