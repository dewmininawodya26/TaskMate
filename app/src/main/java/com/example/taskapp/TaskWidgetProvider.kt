package com.example.taskapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.*

class TaskWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Iterate over all widget instances and update them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // If the widget is updated due to task changes, update it
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(intent.component)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val widgetView = RemoteViews(context.packageName, R.layout.widget_layout)

            // Retrieve tasks and update widget
            val taskPreferences = TaskPreferences(context)
            val upcomingTasks = taskPreferences.loadTasks().filter { task ->
                task.dueDate?.let { dueDate ->
                    isSameDay(dueDate, Date()) || dueDate.after(Date())
                } ?: false
            }

            if (upcomingTasks.isNotEmpty()) {
                val taskTitles = upcomingTasks.joinToString("\n") { it.title }
                widgetView.setTextViewText(R.id.widgetTitle, "Upcoming Tasks (${upcomingTasks.size})") // Show task count
                widgetView.setTextViewText(R.id.widgetTaskList, taskTitles)
            } else {
                widgetView.setTextViewText(R.id.widgetTitle, "Upcoming Tasks (0)") // Show count of 0 tasks
                widgetView.setTextViewText(R.id.widgetTaskList, "No upcoming tasks")
            }

            // Set click event to open the app
            val intent = Intent(context, Home::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            widgetView.setOnClickPendingIntent(R.id.widgetTaskList, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, widgetView)
        }

        // Function to check if two dates are on the same day
        private fun isSameDay(date1: Date, date2: Date): Boolean {
            val calendar1 = Calendar.getInstance().apply { time = date1 }
            val calendar2 = Calendar.getInstance().apply { time = date2 }

            return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                    calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
        }
    }
}
