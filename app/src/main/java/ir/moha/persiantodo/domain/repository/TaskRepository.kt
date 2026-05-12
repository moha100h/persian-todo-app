package ir.moha.persiantodo.domain.repository

import ir.moha.persiantodo.data.local.dao.DateStat
import ir.moha.persiantodo.data.local.entity.CategoryEntity
import ir.moha.persiantodo.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(date: String? = null, categoryId: Long? = null, showCompleted: Boolean = true, query: String = ""): Flow<List<TaskEntity>>
    fun getTasksForDate(date: String): Flow<List<TaskEntity>>
    fun getPendingCount(date: String): Flow<Int>
    fun getCompletedCount(date: String): Flow<Int>
    fun getDateStats(): Flow<List<DateStat>>
    fun getTotalPendingCount(): Flow<Int>
    suspend fun getTaskById(id: Long): TaskEntity?
    suspend fun insertTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
    suspend fun setCompleted(id: Long, done: Boolean)
    suspend fun setPinned(id: Long, pinned: Boolean)
    suspend fun getUpcomingReminders(fromDate: String): List<TaskEntity>
    suspend fun deleteOldCompleted(before: Long)
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity)
}
