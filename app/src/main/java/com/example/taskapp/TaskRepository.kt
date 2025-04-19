package com.example.taskapp

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    suspend fun getAllTasks(): List<Task> {
        return taskDao.getAllTasks()
    }

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)
}
