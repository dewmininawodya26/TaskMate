package com.example.taskapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationReceiver"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DESCRIPTION = "description"
        private const val CHANNEL_ID = "task_notifications"
        private const val CHANNEL_NAME = "Task Notifications"
        private const val CHANNEL_DESCRIPTION = "Channel for task notifications"
        private val VIBRATION_PATTERN = longArrayOf(0, 500, 1000) // Vibration pattern
    }

    data class Task(val title: String)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called")

        // Ensure the notification channel exists
        createNotificationChannel(context)

        // Retrieve title and description from the intent extras
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Task"
        val description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: "Task reminder"

        // Unique notification ID based on the current time
        val notificationId = System.currentTimeMillis().toInt()

        // Intent to launch the main activity (Home)
        val launchIntent = Intent(context, Home::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Build the main notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your notification icon
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setVibrate(VIBRATION_PATTERN) // Vibration pattern
            .setSound(getRingtoneUri(context)) // Set custom ringtone
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Enable all defaults including sound and vibration


        // Create a dummy task
        val task = Task(title)

        // Intent for marking the task as done
        val doneIntent = Intent(context, MarkAsDoneReceiver::class.java).apply {
            putExtra("taskTitle", task.title)
            putExtra("notificationId", notificationId) // Pass notification ID to receiver
        }

        val donePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 2, doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Add action to mark the task as done
        notificationBuilder.addAction(
            R.drawable.done1, "Mark as Ok", donePendingIntent
        )

        // Check for notification permission
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Notification permission not granted")
            return
        }

        // Display the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    // Create the notification channel if it doesn't already exist (for Android O and above)
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // Check if the channel already exists
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = CHANNEL_DESCRIPTION
                    enableVibration(true) // Enable vibration for the channel
                    vibrationPattern = VIBRATION_PATTERN // Set vibration pattern
                    setSound(getRingtoneUri(context), null) // Set default ringtone for the channel
                }

                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            } else {
                Log.d(TAG, "Notification channel already exists")
            }
        }
    }

    // Function to get the ringtone URI
    private fun getRingtoneUri(context: Context): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }
}
