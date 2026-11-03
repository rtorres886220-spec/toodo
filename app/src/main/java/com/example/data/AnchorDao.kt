package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnchorDao {

    @Query("SELECT * FROM anchor_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<AnchorItem>>

    @Query("SELECT * FROM anchor_items WHERE status = 'INBOX' AND type = 'task' AND parentId IS NULL ORDER BY createdAt DESC")
    fun getInboxItems(): Flow<List<AnchorItem>>

    @Query("SELECT * FROM anchor_items WHERE status = 'TODAY' AND type = 'task' AND parentId IS NULL ORDER BY todayOrder ASC, createdAt ASC")
    fun getTodayItems(): Flow<List<AnchorItem>>

    @Query("SELECT * FROM anchor_items WHERE status != 'DONE' AND type = 'task' AND parentId IS NULL AND dueDate IS NOT NULL AND dueDate > :todayDate ORDER BY dueDate ASC, dueTime ASC")
    fun getUpcomingItems(todayDate: String): Flow<List<AnchorItem>>

    @Query("SELECT * FROM anchor_items WHERE type = 'note' AND status != 'DONE' ORDER BY createdAt DESC")
    fun getNotes(): Flow<List<AnchorItem>>

    @Query("SELECT * FROM anchor_items WHERE status = 'DONE' ORDER BY completedAt DESC")
    fun getDoneItems(): Flow<List<AnchorItem>>

    @Query("SELECT * FROM anchor_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): AnchorItem?

    @Query("SELECT * FROM anchor_items WHERE parentId = :parentId ORDER BY id ASC")
    fun getSubtasks(parentId: Long): Flow<List<AnchorItem>>

    @Query("SELECT * FROM anchor_items WHERE parentId = :parentId ORDER BY id ASC")
    suspend fun getSubtasksSync(parentId: Long): List<AnchorItem>

    @Query("DELETE FROM anchor_items WHERE parentId = :parentId")
    suspend fun deleteSubtasksByParentId(parentId: Long)

    @Query("SELECT * FROM anchor_items WHERE status != 'DONE' AND type = 'task' AND dueDate IS NOT NULL AND (dueDate < :currentDate OR (dueDate = :currentDate AND dueTime IS NOT NULL AND dueTime < :currentTime)) ORDER BY dueDate ASC, dueTime ASC")
    suspend fun getOverdueItems(currentDate: String, currentTime: String): List<AnchorItem>

    @Query("SELECT * FROM anchor_items WHERE status != 'DONE' AND type = 'task' AND dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC, dueTime ASC")
    fun getItemsForDateRange(startDate: String, endDate: String): Flow<List<AnchorItem>>

    @Query("SELECT * FROM anchor_items WHERE status != 'DONE' AND type = 'task' AND snoozeCount >= :minSnoozes ORDER BY snoozeCount DESC")
    fun getHabitualSnoozedItems(minSnoozes: Int = 3): Flow<List<AnchorItem>>

    @Query("UPDATE anchor_items SET type = :type, title = :title, dueDate = :dueDate, dueTime = :dueTime, priority = :priority, tags = :tags, estimatedMinutes = :estimatedMinutes, aiSorted = 1 WHERE id = :id AND status != 'DONE' AND aiSorted = 0")
    suspend fun updateAiEnrichment(
        id: Long,
        type: String,
        title: String,
        dueDate: String?,
        dueTime: String?,
        priority: String,
        tags: String,
        estimatedMinutes: Int?
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AnchorItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AnchorItem>): List<Long>

    @Update
    suspend fun update(item: AnchorItem)

    @Delete
    suspend fun delete(item: AnchorItem)

    @Query("DELETE FROM anchor_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM anchor_items WHERE status = 'DONE' AND completedAt >= :sinceTimestamp")
    suspend fun countDoneSince(sinceTimestamp: Long): Int

    @Query("SELECT * FROM anchor_items WHERE status != 'DONE' AND type = 'task'")
    suspend fun getAllActiveTasksSync(): List<AnchorItem>

    @Query("SELECT * FROM anchor_items WHERE status != 'DONE' ORDER BY createdAt DESC")
    suspend fun getAllActiveItemsSync(): List<AnchorItem>

    @Query("SELECT * FROM anchor_items WHERE type = 'note' AND status != 'DONE' AND (title LIKE '%' || :query || '%' OR rawInput LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    suspend fun searchNotesSync(query: String): List<AnchorItem>

    @Query("SELECT * FROM anchor_items WHERE parentId IS NULL AND (title LIKE '%' || :query || '%' OR rawInput LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchAllItems(query: String): Flow<List<AnchorItem>>
}
