package ir.moha.persiantodo.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.moha.persiantodo.data.local.entity.TaskEntity
import ir.moha.persiantodo.util.JalaliCalendar
import java.util.Calendar

object ReminderScheduler {

    fun schedule(context: Context, task: TaskEntity) {
        val date   = task.jalaliDate   ?: return
        val time   = task.reminderTime ?: return
        val jalali = JalaliCalendar.parse(date) ?: return
        val (gy, gm, gd) = jalali.toGregorian()
        val parts  = time.split(":")
        if (parts.size < 2) return
        val hour   = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val triggerTime = Calendar.getInstance().apply {
            set(gy, gm - 1, gd, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (triggerTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        // Android 12+ نیاز به مجوز SCHEDULE_EXACT_ALARM دارد
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ir.moha.persiantodo.ALARM_ACTION"
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
        }
        val pending = PendingIntent.getBroadcast(
            context, task.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pending)
    }

    fun cancel(context: Context, taskId: Long) {
        val intent  = Intent(context, AlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, taskId.toInt(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pending?.let {
            context.getSystemService(AlarmManager::class.java).cancel(it)
            it.cancel()
        }
    }
}
