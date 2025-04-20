package com.example.taskapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.taskapp.data.model.Task

@Database(entities = [Task::class], exportSchema = false, version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}