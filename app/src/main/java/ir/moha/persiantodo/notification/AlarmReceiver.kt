package ir.moha.persiantodo.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.EntryPointAccessors
import ir.moha.persiantodo.MainActivity
import ir.moha.persiantodo.PersianTodoApp
import ir.moha.persiantodo.R
import ir.moha.persiantodo.di.AlarmReceiverEntryPoint
import ir.moha.persiantodo.util.JalaliCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // دریافت repository از طریق EntryPoint (بدون @AndroidEntryPoint)
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmReceiverEntryPoint::class.java
        )
        val repository = entryPoint.taskRepository()

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // بعد از ریبوت، همه یادآورها را دوباره ست کن
                CoroutineScope(Dispatchers.IO).launch {
                    val today = JalaliCalendar.now().toString()
                    val tasks = repository.getUpcomingReminders(today)
                    tasks.forEach { task -> ReminderScheduler.schedule(context, task) }
                }
            }
            else -> {
                // نمایش نوتیفیکیشن یادآور
                val taskId    = intent.getLongExtra("task_id", -1L)
                val taskTitle = intent.getStringExtra("task_title") ?: "یادآور وظیفه"

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    taskId.toInt(),
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("task_id", taskId)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, PersianTodoApp.CHANNEL_REMINDER)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("⏰ یادآور")
                    .setContentText(taskTitle)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(longArrayOf(0, 300, 100, 300))
                    .build()

                val manager = context.getSystemService(NotificationManager::class.java)
                manager.notify(taskId.toInt(), notification)
            }
        }
    }
}
