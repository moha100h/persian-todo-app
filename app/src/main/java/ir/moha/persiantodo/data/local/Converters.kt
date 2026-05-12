package ir.moha.persiantodo.data.local

import androidx.room.TypeConverter
import ir.moha.persiantodo.data.local.entity.Priority
import ir.moha.persiantodo.data.local.entity.RepeatType

class Converters {

    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority =
        runCatching { Priority.valueOf(value) }.getOrDefault(Priority.NORMAL)

    @TypeConverter
    fun fromRepeatType(value: RepeatType): String = value.name

    @TypeConverter
    fun toRepeatType(value: String): RepeatType =
        runCatching { RepeatType.valueOf(value) }.getOrDefault(RepeatType.NONE)
}
