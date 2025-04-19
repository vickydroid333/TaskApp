package com.example.taskapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    val allTasks: StateFlow<List<Task>> = _allTasks

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task


    val taskName = MutableLiveData("")
    val taskDescription = MutableLiveData("")
    val taskDate = MutableLiveData("")
    val taskTime = MutableLiveData("")
    val taskCategory = MutableLiveData("")
    val taskPriority = MutableLiveData(1)

    fun insertTask() {
        viewModelScope.launch {
            val task = Task(
                name = taskName.value ?: "",
                description = taskDescription.value ?: "",
                dueDate = taskDate.value ?: "",
                dueTime = taskTime.value ?: "",
                category = taskCategory.value ?: "",
                priority = taskPriority.value ?: 1
            )
            repository.insertTask(task)
        }
    }

    fun loadAllTasks() {
        viewModelScope.launch {
            _allTasks.value = repository.getAllTasks()
        }
    }

    fun loadTaskById(id: Int) {
        viewModelScope.launch {
            repository.getTaskById(id).collect { taskEntity ->
                _task.value = taskEntity
            }
        }
    }

}
