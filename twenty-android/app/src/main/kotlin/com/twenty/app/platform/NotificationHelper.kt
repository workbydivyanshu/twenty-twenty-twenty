package com.twenty.app.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.twenty.app.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "twenty_timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.twenty.app.ACTION_START"
        const val ACTION_STOP = "com.twenty.app.ACTION_STOP"
        const val ACTION_BREAK_CONFIRM = "com.twenty.app.ACTION_BREAK_CONFIRM"
        const val ACTION_BREAK_SKIP = "com.twenty.app.ACTION_BREAK_SKIP"
        const val ACTION_BREAK_TAKE = "com.twenty.app.ACTION_BREAK_TAKE"
        const val ACTION_BREAK_SKIP_SESSION = "com.twenty.app.ACTION_BREAK_SKIP_SESSION"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows timer progress"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        title: String,
        content: String,
        isBreakPending: Boolean = false,
        isBreakActive: Boolean = false,
        isSessionActive: Boolean = false,
        isBreakConfirmPending: Boolean = false
    ): Notification {
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)

        when {
            isBreakConfirmPending -> {
                builder.addAction(
                    android.R.drawable.ic_menu_view,
                    "Yes, I did",
                    createActionPendingIntent(ACTION_BREAK_CONFIRM)
                )
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "No, I didn't",
                    createActionPendingIntent(ACTION_BREAK_SKIP)
                )
            }
            isBreakPending -> {
                builder.addAction(
                    android.R.drawable.ic_menu_view,
                    "Take Break",
                    createActionPendingIntent(ACTION_BREAK_TAKE)
                )
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Skip Session",
                    createActionPendingIntent(ACTION_BREAK_SKIP_SESSION)
                )
            }
            isBreakActive -> {
                builder.addAction(
                    android.R.drawable.ic_menu_view,
                    "View",
                    createActionPendingIntent(ACTION_BREAK_CONFIRM)
                )
            }
            isSessionActive -> {
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop",
                    createActionPendingIntent(ACTION_STOP)
                )
            }
        }

        return builder.build()
    }

    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(action).apply {
            setPackage(context.packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode() and 0x7FFFFFFF,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
