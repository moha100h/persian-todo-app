package ir.moha.persiantodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.moha.persiantodo.data.local.dao.CategoryDao
import ir.moha.persiantodo.data.local.dao.TaskDao
import ir.moha.persiantodo.data.local.entity.CategoryEntity
import ir.moha.persiantodo.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false          // false → نیازی به schema dir نیست
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
}
