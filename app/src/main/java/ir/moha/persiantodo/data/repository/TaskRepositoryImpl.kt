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

    override fun getTasks(date: String?, categoryId: Long?, showCompleted: Boolean, query: String): Flow<List<TaskEntity>> =
        taskDao.getTasks(date, categoryId, showCompleted, query)

    override fun getPendingCount(date: String): Flow<Int> =
        taskDao.getPendingCountForDate(date)

    override fun getCompletedCount(date: String): Flow<Int> =
        taskDao.getCompletedCountForDate(date)

    override fun getDateStats(): Flow<List<DateStat>> =
        taskDao.getDateStats()

    override fun getTotalPendingCount(): Flow<Int> =
        taskDao.getTotalPendingCount()

    override fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    override suspend fun getTaskById(id: Long): TaskEntity? =
        taskDao.getTaskById(id)

    override suspend fun insertTask(task: TaskEntity): Long =
        taskDao.insertTask(task)

    override suspend fun updateTask(task: TaskEntity) =
        taskDao.updateTask(task)

    override suspend fun deleteTask(task: TaskEntity) =
        taskDao.deleteTask(task)

    override suspend fun setCompleted(id: Long, done: Boolean) =
        taskDao.setCompleted(id, done)

    override suspend fun setPinned(id: Long, pinned: Boolean) =
        taskDao.setPinned(id, pinned)

    override suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    override suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)
}
