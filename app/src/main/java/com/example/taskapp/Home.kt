package com.example.taskapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class Home : AppCompatActivity() {
    private lateinit var rvUpcomingTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskPreferences: TaskPreferences
    private lateinit var alarmManager: AlarmManager

    private val REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Handle window insets for padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bottom navigation setup
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_task -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.nav_timer -> {
                    startActivity(Intent(this, TimerActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.nav_calender -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                else -> false
            }
        }

        rvUpcomingTasks = findViewById(R.id.rvUpcomingTasks)
        taskPreferences = TaskPreferences(this)

        // Request notification permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
        } else {
            createNotificationChannel()
        }

        setupUpcomingTasksRecyclerView()
        updateWidget() // Call to ensure widget is updated with current tasks
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createNotificationChannel()
            } else {
                Log.d("Home", "Notification permission denied")
                Toast.makeText(this, "Notification permission is required to show task reminders.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_notifications",
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for task notifications"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupUpcomingTasksRecyclerView() {
        val allTasks = getAllTasks()
        val currentDateTime = Date()

        // Set the time boundaries for today
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startOfToday = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfToday = calendar.time

        // Filter upcoming tasks
        val upcomingTasks = allTasks.filter { task ->
            task.dueDate?.let { dueDate ->
                dueDate.after(currentDateTime) ||
                        (dueDate.after(startOfToday) && dueDate.before(endOfToday))
            } ?: false
        }.toMutableList()

        // Schedule reminders for tasks that are due soon or now
        upcomingTasks.forEach { task ->
            scheduleReminder(task)
        }

        taskAdapter = TaskAdapter(upcomingTasks, this, { position ->
            // Handle task click
        }, { position ->
            // Handle delete task
            deleteTask(position)
        })
        rvUpcomingTasks.adapter = taskAdapter
        rvUpcomingTasks.layoutManager = LinearLayoutManager(this)
    }

    private fun getAllTasks(): List<Task> {
        return taskPreferences.loadTasks()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleReminder(task: Task) {
        val notificationId = System.currentTimeMillis().toInt()
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("title", task.title)
            putExtra("description", "Your task '${task.title}' is due soon!")
            putExtra("dueDate", task.dueDate?.time ?: 0)
        }

        // Schedule "Due Soon" Notification (15 minutes before)
        task.dueDate?.let { dueDate ->
            val dueSoonTime = dueDate.time - 15 * 60 * 1000 // 15 minutes before due time
            if (dueSoonTime > System.currentTimeMillis()) {
                val pendingIntent = PendingIntent.getBroadcast(
                    this, notificationId,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    dueSoonTime,
                    pendingIntent
                )
            }

            // Schedule "Due Now" Notification
            intent.putExtra("description", "Your task '${task.title}' is now due!")
            val pendingIntentNow = PendingIntent.getBroadcast(
                this, notificationId + 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                dueDate.time,
                pendingIntentNow
            )
        }
    }

    private fun deleteTask(position: Int) {
        val taskToDelete = taskAdapter.getTasks()[position]
        taskAdapter.deleteTask(position)
        val allTasks = getAllTasks().toMutableList()
        allTasks.remove(taskToDelete)  // Remove from the list in preferences
        taskPreferences.saveTasks(allTasks)  // Update the preferences
        updateWidget() // Call to update the widget after task deletion
    }

    private fun updateWidget() {
        val intent = Intent(this, TaskWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, TaskWidgetProvider::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

}
