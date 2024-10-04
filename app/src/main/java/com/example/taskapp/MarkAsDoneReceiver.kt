package com.example.taskapp

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MarkAsDoneReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Get the task title and notification ID from the intent extras
        val taskTitle = intent.getStringExtra("taskTitle")
        val notificationId = intent.getIntExtra("notificationId", -1)

        if (taskTitle != null) {
            // Perform the task completion logic here using the task title
            markTaskAsDone(context, taskTitle)

            // Show a toast indicating the task is marked as done
            Toast.makeText(context, "Task '$taskTitle' marked as ok!", Toast.LENGTH_SHORT).show()

            // Cancel the notification using the NotificationManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        } else {
            Log.e("MarkAsDoneReceiver", "Task title is null")
        }
    }

    // Dummy function to simulate task completion using the title
    private fun markTaskAsDone(context: Context, taskTitle: String) {
        // Perform your logic to mark the task as done using the title
        Log.d("MarkAsDoneReceiver", "Task with title '$taskTitle' marked as ok.")
    }
}
