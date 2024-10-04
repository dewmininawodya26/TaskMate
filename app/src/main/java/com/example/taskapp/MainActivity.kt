package com.example.taskapp

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var rvTasks: RecyclerView
    private lateinit var btnAddTask: Button
    private lateinit var taskPreferences: TaskPreferences
    private lateinit var bottomNavigationView: BottomNavigationView

    companion object {
        const val ADD_TASK_REQUEST_CODE = 1
        const val EDIT_TASK_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TaskPreferences
        taskPreferences = TaskPreferences(this)

        // Find views
        rvTasks = findViewById(R.id.rvTasks)
        btnAddTask = findViewById(R.id.btnAddTask)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set up RecyclerView
        setupRecyclerView()

        // Add task button click listener
        btnAddTask.setOnClickListener {
            openTaskDetailsForResult(ADD_TASK_REQUEST_CODE)
        }

        // Bottom navigation item selection listener
        setupBottomNavigationView()
    }

    private fun setupRecyclerView() {
        // Load all tasks
        taskAdapter = TaskAdapter(
            getAllTasks(),
            this,
            { position -> openTaskDetailsForResult(EDIT_TASK_REQUEST_CODE, position) },
            { position -> deleteTask(position) }
        )
        rvTasks.adapter = taskAdapter
        rvTasks.layoutManager = LinearLayoutManager(this)
    }

    private fun getAllTasks(): MutableList<Task> {
        // Fetch all tasks
        return taskPreferences.loadTasks().toMutableList()
    }

    private fun openTaskDetailsForResult(requestCode: Int, position: Int? = null) {
        val intent = Intent(this, TaskDetailsActivity::class.java).apply {
            position?.let {
                val task = taskAdapter.getTasks()[it]
                putExtra("task_position", it)
                putExtra("is_editing", true)
                putExtra("task_title", task.title)
                putExtra("task_description", task.description)
                putExtra("task_due_date", task.dueDate?.time) // Pass due date
                putExtra("task_due_time", task.dueTime?.time) // Pass due time
                putExtra("task_priority", task.priority)
                putExtra("task_is_done", task.isDone)
            }
        }
        startActivityForResult(intent, requestCode)
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_task -> true // Set nav_task as the current view
                R.id.nav_timer -> {
                    startActivity(Intent(this, TimerActivity::class.java))
                    true
                }
                R.id.nav_calender -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                else -> false
            }
        }
        // Set the initial selected item
        bottomNavigationView.selectedItemId = R.id.nav_task
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                ADD_TASK_REQUEST_CODE -> handleTaskCreation(data)
                EDIT_TASK_REQUEST_CODE -> handleTaskEdit(data)
            }
        }
    }

    private fun handleTaskCreation(data: Intent) {
        val taskTitle = data.getStringExtra("task_title")
        val taskDescription = data.getStringExtra("task_description")
        val dueDateMillis = data.getLongExtra("task_due_date", -1L)
        val dueTimeMillis = data.getLongExtra("task_due_time", -1L) // Added due time
        val taskPriority = data.getIntExtra("task_priority", 0)
        val isDone = data.getBooleanExtra("task_is_done", false)

        if (taskTitle != null) {
            val task = Task(
                title = taskTitle,
                description = taskDescription ?: "",
                dueDate = if (dueDateMillis != -1L) Date(dueDateMillis) else null,
                dueTime = if (dueTimeMillis != -1L) Date(dueTimeMillis) else null, // Handle due time
                priority = taskPriority,
                isDone = isDone
            )
            taskAdapter.addTask(task)
            taskPreferences.saveTasks(taskAdapter.getTasks())
            notifyWidgetUpdate() // Notify the widget to update
        } else {
            Toast.makeText(this, "Task creation failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleTaskEdit(data: Intent) {
        val position = data.getIntExtra("task_position", -1)
        val taskTitle = data.getStringExtra("task_title")
        val taskDescription = data.getStringExtra("task_description")
        val dueDateMillis = data.getLongExtra("task_due_date", -1L)
        val dueTimeMillis = data.getLongExtra("task_due_time", -1L) // Handle due time
        val taskPriority = data.getIntExtra("task_priority", 0)
        val isDone = data.getBooleanExtra("task_is_done", false)

        if (position >= 0 && taskTitle != null) {
            val task = taskAdapter.getTasks()[position]
            task.title = taskTitle
            task.description = taskDescription ?: task.description
            task.dueDate = if (dueDateMillis != -1L) Date(dueDateMillis) else task.dueDate
            task.dueTime = if (dueTimeMillis != -1L) Date(dueTimeMillis) else task.dueTime // Update due time
            task.priority = taskPriority
            task.isDone = isDone
            taskAdapter.updateTask(position, task)
            taskPreferences.saveTasks(taskAdapter.getTasks())
            notifyWidgetUpdate() // Notify the widget to update
        } else {
            Toast.makeText(this, "Task update failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask(position: Int) {
        taskAdapter.deleteTask(position)
        taskPreferences.saveTasks(taskAdapter.getTasks())
        notifyWidgetUpdate() // Notify the widget to update
    }

    private fun notifyWidgetUpdate() {
        val intent = Intent(this, TaskWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        sendBroadcast(intent)
    }
}
