package ir.moha.persiantodo.data.local.dao

import androidx.room.*
import ir.moha.persiantodo.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

data class DateStat(
    val jalaliDate: String,
    val total: Int,
    val completed: Int
)

@Dao
interface TaskDao {

    @Query("""
        SELECT * FROM tasks
        WHERE (:date IS NULL OR jalaliDate = :date)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:showCompleted = 1 OR isCompleted = 0)
          AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY isPinned DESC,
                 CASE priority WHEN 'URGENT' THEN 0 WHEN 'HIGH' THEN 1 WHEN 'NORMAL' THEN 2 ELSE 3 END,
                 sortOrder ASC, createdAt DESC
    """)
    fun getTasks(
        date: String? = null,
        categoryId: Long? = null,
        showCompleted: Boolean = true,
        query: String = ""
    ): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT COUNT(*) FROM tasks WHERE jalaliDate = :date AND isCompleted = 0")
    fun getPendingCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE jalaliDate = :date AND isCompleted = 1")
    fun getCompletedCountForDate(date: String): Flow<Int>

    @Query("""
        SELECT jalaliDate,
               COUNT(*) as total,
               SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as completed
        FROM tasks
        WHERE jalaliDate IS NOT NULL
        GROUP BY jalaliDate
    """)
    fun getDateStats(): Flow<List<DateStat>>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getTotalPendingCount(): Flow<Int>

    @Query("SELECT * FROM tasks WHERE reminderTime IS NOT NULL AND isCompleted = 0 AND jalaliDate >= :fromDate")
    suspend fun getUpcomingReminders(fromDate: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :done, completedAt = CASE WHEN :done = 1 THEN strftime('%s','now') * 1000 ELSE NULL END WHERE id = :id")
    suspend fun setCompleted(id: Long, done: Boolean)

    @Query("UPDATE tasks SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteAllCompleted()
}
