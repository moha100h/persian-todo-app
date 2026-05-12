package ir.moha.persiantodo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PersianTodoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // کانال یادآور وظایف
            NotificationChannel(
                CHANNEL_REMINDER,
                "یادآور وظایف",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "نوتیفیکیشن یادآور وظایف روزانه"
                enableVibration(true)
                manager.createNotificationChannel(this)
            }

            // کانال خلاصه روزانه
            NotificationChannel(
                CHANNEL_DAILY,
                "خلاصه روزانه",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "خلاصه وظایف روز"
                manager.createNotificationChannel(this)
            }
        }
    }

    companion object {
        const val CHANNEL_REMINDER = "reminder_channel"
        const val CHANNEL_DAILY    = "daily_channel"
    }
}
