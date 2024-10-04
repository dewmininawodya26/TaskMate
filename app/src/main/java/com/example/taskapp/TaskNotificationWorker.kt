package com.example.taskapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Date

class TaskNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val upcomingTasks = getUpcomingTasks() // Retrieve upcoming tasks from preferences
        if (upcomingTasks.isNotEmpty()) {
            sendNotification(upcomingTasks)
        }
        return Result.success()
    }

    private fun getUpcomingTasks(): List<Task> {
        val taskPreferences = TaskPreferences(applicationContext)
        return taskPreferences.loadTasks().filter { task ->
            (task.dueDate?.after(Date()) == true || task.dueDate == null) // Adjusted logic
        }.sortedBy { it.dueDate }
    }


    private fun sendNotification(tasks: List<Task>) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_notifications"
        val channelName = "Task Notifications"

        // Create a notification channel (for API 26+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Your notification icon
            .setContentTitle("Upcoming Tasks")
            .setContentText("You have upcoming tasks: ${tasks.joinToString(", ") { it.title }}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(1, notificationBuilder.build())
    }
}
