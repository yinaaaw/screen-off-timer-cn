package com.inod.screenofftimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

const val DEFAULT = "timer"

class ScreenOffTimerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    DEFAULT,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.notification_channel_desc)
                    setSound(null, null)
                    enableVibration(false)
                }

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)

        }
    }
}
