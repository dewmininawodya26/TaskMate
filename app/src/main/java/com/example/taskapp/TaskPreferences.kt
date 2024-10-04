package com.example.taskapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class TaskPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        .create()

    fun saveTasks(taskList: List<Task>) {
        val taskListJson = gson.toJson(taskList)
        sharedPreferences.edit().putString("task_list", taskListJson).apply()
    }

    fun loadTasks(): List<Task> {
        val taskListJson = sharedPreferences.getString("task_list", null)
        return if (taskListJson != null) {
            try {
                val type = object : TypeToken<List<Task>>() {}.type
                gson.fromJson(taskListJson, type)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            emptyList()
        }
    }
//    fun removeTask(task: Task) {
//        val tasks = loadTasks().toMutableList()
//        tasks.remove(task)
//        saveTasks(tasks) // Method to save the updated task list
//    }

    fun deleteTask(task: Task) {
        val tasks = loadTasks().toMutableList()
        tasks.removeIf { it.title == task.title && it.description == task.description }
        saveTasks(tasks)
    }


}
