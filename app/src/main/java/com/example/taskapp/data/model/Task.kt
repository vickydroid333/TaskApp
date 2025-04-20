package com.example.taskapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val dueDate: String,
    val dueTime: String,
    val category: String,
    val priority: Int,
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false
)