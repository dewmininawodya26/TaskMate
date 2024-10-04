package com.example.taskapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private var tasks: MutableList<Task>,
    private val context: Context,
    private val onTaskClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        private val taskDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        private val taskDueDate: TextView = itemView.findViewById(R.id.tvTaskDueDate)
        private val taskPriority: TextView = itemView.findViewById(R.id.tvTaskPriority)
        private val taskIsDone: CheckBox = itemView.findViewById(R.id.cbTaskIsDone)
        private val btnDeleteTask: ImageButton = itemView.findViewById(R.id.btnDeleteTask)

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        fun bind(task: Task, position: Int) {
            taskTitle.text = task.title
            taskDescription.text = task.description
            taskDueDate.text = "${task.dueDate?.let { dateFormat.format(it) } ?: "No due date"}, ${task.dueTime?.let { timeFormat.format(it) } ?: "No due time"}"
            taskPriority.text = "Priority: ${task.priority}"
            taskIsDone.isChecked = task.isDone

            itemView.setOnClickListener {
                onTaskClick(position)
            }

            taskIsDone.setOnCheckedChangeListener { _, isChecked ->
                task.isDone = isChecked
            }

            btnDeleteTask.setOnClickListener {
                showDeleteConfirmationDialog(position)
            }
        }

        private fun showDeleteConfirmationDialog(position: Int) {
            AlertDialog.Builder(context)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes") { _, _ -> onDeleteClick(position) }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], position)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    fun updateTaskList(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    fun updateTask(position: Int, task: Task) {
        tasks[position] = task
        notifyItemChanged(position)
    }

    fun deleteTask(position: Int) {
        tasks.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getTasks(): MutableList<Task> {
        return tasks
    }
    fun removeTask(position: Int) {
        tasks.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, tasks.size)
    }

}
