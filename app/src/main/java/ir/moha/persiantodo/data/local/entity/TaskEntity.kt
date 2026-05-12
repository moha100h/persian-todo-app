package ir.moha.persiantodo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Priority { LOW, NORMAL, HIGH, URGENT }
enum class RepeatType { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }

@Entity(
    tableName = "tasks",
    indices = [
        Index("jalaliDate"),
        Index("isCompleted"),
        Index("categoryId"),
        Index("priority")
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: Long? = null,
    val priority: Priority = Priority.NORMAL,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val jalaliDate: String? = null,       // YYYY-MM-DD
    val reminderTime: String? = null,     // HH:mm
    val repeatType: RepeatType = RepeatType.NONE,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val sortOrder: Int = 0
)
