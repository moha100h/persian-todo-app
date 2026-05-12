package ir.moha.persiantodo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority { LOW, NORMAL, HIGH, URGENT }
enum class RepeatType { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: Long? = null,
    val priority: Priority = Priority.NORMAL,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    // تاریخ شمسی به فرمت YYYY-MM-DD
    val jalaliDate: String? = null,
    // زمان یادآور به فرمت HH:mm
    val reminderTime: String? = null,
    val repeatType: RepeatType = RepeatType.NONE,
    val tags: String = "",          // JSON آرایه تگ‌ها
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val sortOrder: Int = 0
)
