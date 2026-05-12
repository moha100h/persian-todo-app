package ir.moha.persiantodo.data.local

import androidx.room.TypeConverter
import ir.moha.persiantodo.data.local.entity.Priority
import ir.moha.persiantodo.data.local.entity.RepeatType

class Converters {
    @TypeConverter fun fromPriority(p: Priority): String = p.name
    @TypeConverter fun toPriority(s: String): Priority = Priority.valueOf(s)
    @TypeConverter fun fromRepeat(r: RepeatType): String = r.name
    @TypeConverter fun toRepeat(s: String): RepeatType = RepeatType.valueOf(s)
}
