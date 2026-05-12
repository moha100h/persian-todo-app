package ir.moha.persiantodo.data.repository

import ir.moha.persiantodo.data.local.dao.CategoryDao
import ir.moha.persiantodo.data.local.dao.DateStat
import ir.moha.persiantodo.data.local.dao.TaskDao
import ir.moha.persiantodo.data.local.entity.CategoryEntity
import ir.moha.persiantodo.data.local.entity.TaskEntity
import ir.moha.persiantodo.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao
) : TaskRepository {
    override fun getTasks(date: String?, categoryId: Long?, showCompleted: Boolean, query: String) =
        taskDao.getTasks(date, categoryId, showCompleted, query)
    override fun getTasksForDate(date: String) = taskDao.getTasksForDate(date)
    override fun getPendingCount(date: String) = taskDao.getPendingCountForDate(date)
    override fun getCompletedCount(date: String) = taskDao.getCompletedCountForDate(date)
    override fun getDateStats() = taskDao.getDateStats()
    override fun getTotalPendingCount() = taskDao.getTotalPendingCount()
    override suspend fun getTaskById(id: Long) = taskDao.getTaskById(id)
    override suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)
    override suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    override suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    override suspend fun setCompleted(id: Long, done: Boolean) = taskDao.setCompleted(id, done)
    override suspend fun setPinned(id: Long, pinned: Boolean) = taskDao.setPinned(id, pinned)
    override suspend fun getUpcomingReminders(fromDate: String) = taskDao.getUpcomingReminders(fromDate)
    override suspend fun deleteOldCompleted(before: Long) = taskDao.deleteOldCompleted(before)
    override fun getAllCategories() = categoryDao.getAllCategories()
    override suspend fun insertCategory(c: CategoryEntity) = categoryDao.insertCategory(c)
    override suspend fun updateCategory(c: CategoryEntity) = categoryDao.updateCategory(c)
    override suspend fun deleteCategory(c: CategoryEntity) = categoryDao.deleteCategory(c)
}
