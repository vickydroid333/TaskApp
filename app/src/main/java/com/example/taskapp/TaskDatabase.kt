package com.example.taskapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Task::class], exportSchema = false, version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
