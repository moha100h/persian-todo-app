package ir.moha.persiantodo.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ir.moha.persiantodo.data.local.entity.TaskEntity
import ir.moha.persiantodo.util.JalaliCalendar
import java.util.Calendar

object ReminderScheduler {

    fun schedule(context: Context, task: TaskEntity) {
        val date = task.jalaliDate ?: return
        val time = task.reminderTime ?: return
        val jalali = JalaliCalendar.parse(date) ?: return
        val (gy, gm, gd) = jalali.toGregorian()
        val (hour, minute) = time.split(":").map { it.toInt() }

        val triggerTime = Calendar.getInstance().apply {
            set(gy, gm - 1, gd, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ir.moha.persiantodo.ALARM_ACTION"
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
        }
        val pending = PendingIntent.getBroadcast(
            context, task.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pending)
    }

    fun cancel(context: Context, taskId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
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
