package com.example.taskapp

import java.util.*

import java.io.Serializable

data class Task(
    var title: String,
    var description: String,
    var dueDate: Date?,
    var dueTime: Date?,
    var priority: Int,
    var isDone: Boolean = false,
    var reminderTime: Long = 0L
) : Serializable


