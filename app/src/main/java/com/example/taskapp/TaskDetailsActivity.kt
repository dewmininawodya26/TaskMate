package com.example.taskapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvDueDate: TextView
    private lateinit var tvDueTime: TextView // New TextView for due time
    private lateinit var spPriority: Spinner
    private lateinit var btnSave: Button
    private var dueDate: Date? = null
    private var dueTime: Date? = null // New variable for due time
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Format for time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        etTitle = findViewById(R.id.etTaskTitle)
        etDescription = findViewById(R.id.etTaskDescription)
        tvDueDate = findViewById(R.id.tvTaskDueDate)
        tvDueTime = findViewById(R.id.tvTaskDueTime)
        spPriority = findViewById(R.id.spPriority)
        btnSave = findViewById(R.id.btnSave)

        val isEditing = intent.getBooleanExtra("is_editing", false)
        if (isEditing) {
            setupEditingMode()
        } else {
            setupCreationMode()
        }

        tvDueDate.setOnClickListener {
            showDatePicker()
        }

        tvDueTime.setOnClickListener {
            showTimePicker() // Show time picker when due time TextView is clicked
        }

        btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun setupEditingMode() {
        val position = intent.getIntExtra("task_position", -1)
        etTitle.setText(intent.getStringExtra("task_title"))
        etDescription.setText(intent.getStringExtra("task_description"))
        dueDate = Date(intent.getLongExtra("task_due_date", -1))
        dueDate?.let { tvDueDate.text = dateFormat.format(it) }
        dueTime = Date(intent.getLongExtra("task_due_time", -1)) // Retrieve due time
        dueTime?.let { tvDueTime.text = timeFormat.format(it) }
        spPriority.setSelection(intent.getIntExtra("task_priority", 0))
    }


    private fun setupCreationMode() {
        tvDueDate.text = "Select due date"
        tvDueTime.text = "Select due time" // Default text for due time
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            dueDate = selectedDate.time
            tvDueDate.text = dateFormat.format(dueDate!!)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hourOfDay, minute ->
            val selectedTime = Calendar.getInstance()
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTime.set(Calendar.MINUTE, minute)
            dueTime = selectedTime.time
            tvDueTime.text = timeFormat.format(dueTime!!)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }

    private fun saveTask() {
        val taskTitle = etTitle.text.toString()
        val taskDescription = etDescription.text.toString()
        val taskPriority = spPriority.selectedItemPosition
        val taskIsDone = false

        if (taskTitle.isEmpty()) {
            Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent().apply {
            putExtra("task_title", taskTitle)
            putExtra("task_description", taskDescription)
            putExtra("task_due_date", dueDate?.time)
            putExtra("task_due_time", dueTime?.time) // Pass due time
            putExtra("task_priority", taskPriority)
            putExtra("task_is_done", taskIsDone)

            if (intent.getBooleanExtra("is_editing", false)) {
                putExtra("task_position", intent.getIntExtra("task_position", -1))
            }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
