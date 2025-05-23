package com.example.taskapp.ui.activity

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskapp.R
import com.example.taskapp.data.model.Task
import com.example.taskapp.databinding.ActivityMainBinding
import com.example.taskapp.ui.adapter.OnTaskClickListener
import com.example.taskapp.ui.adapter.TaskAdapter
import com.example.taskapp.ui.viewmodel.AddEditTaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnTaskClickListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AddEditTaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var completedDeletedAdapter: TaskAdapter

    private var originalTaskList: List<Task> = emptyList()

    private val addEditTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.loadAllTasks()
        if (result.resultCode == RESULT_OK) {
            val showOnlyDeleted = result.data?.getBooleanExtra("showOnlyDeleted", false) ?: false

            if (showOnlyDeleted) {
                showOnlyDeletedTasksAndResetUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSpinners()
        setupSearch()

        lifecycleScope.launchWhenStarted {
            viewModel.allTasks.collectLatest { tasks ->
                originalTaskList = tasks
                filterTasks()
            }
        }

        viewModel.loadAllTasks()

        binding.fabAddTask.setOnClickListener {
            val intent = Intent(this, AddEditTaskActivity::class.java)
            addEditTaskLauncher.launch(intent)
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(emptyList(), this)
        completedDeletedAdapter = TaskAdapter(emptyList(), this)

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTasks.adapter = taskAdapter

        binding.recyclerViewCompletedDeleted.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCompletedDeleted.adapter = completedDeletedAdapter
    }

    override fun onTaskClick(taskId: Int) {
        val intent = Intent(this, TaskDetailActivity::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("isEdit", true)
        }
        addEditTaskLauncher.launch(intent)
    }

    override fun onTaskCompleteConfirm(task: Task) {
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("Complete Task")
            .setMessage("Are you sure you want to mark '${task.name}' as completed?")
            .setPositiveButton("Yes") { _, _ ->
                // Mark as completed
                val updatedTask = task.copy(isCompleted = true)
                viewModel.updateTask(updatedTask)
            }
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }

    override fun onTaskIncompleteConfirm(task: Task) {
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("Mark as Incomplete")
            .setMessage("Are you sure you want to mark '${task.name}' as incomplete?")
            .setPositiveButton("Yes") { _, _ ->
                val updatedTask = task.copy(isCompleted = false)
                viewModel.updateTask(updatedTask)
            }
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }

    private fun showOnlyDeletedTasksAndResetUI() {
        val deletedTasks = originalTaskList.filter { it.isDeleted }
        completedDeletedAdapter.updateTasks(deletedTasks)
        binding.spinnerTaskFilter.setSelection(0)
        binding.spinnerStatusFilter.setSelection(1)
        binding.editTextSearch.setText("")

        taskAdapter.updateTasks(emptyList())
        completedDeletedAdapter.updateTasks(deletedTasks)
    }

    private fun setupSpinners() {
        val taskFilterItems = listOf("All Tasks", "Today Tasks")
        val statusFilterItems = listOf("Completed Tasks", "Deleted Tasks")

        val adapter = ArrayAdapter(this, R.layout.spinner_item, taskFilterItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTaskFilter.adapter = adapter

        val adapter1 = ArrayAdapter(this, R.layout.spinner_item, statusFilterItems)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatusFilter.adapter = adapter1

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) = filterTasks()

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerTaskFilter.onItemSelectedListener = listener
        binding.spinnerStatusFilter.onItemSelectedListener = listener
    }

    private fun setupSearch() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTasks()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterTasks() {
        val query = binding.editTextSearch.text.toString()
        val taskFilter = binding.spinnerTaskFilter.selectedItem.toString()
        val statusFilter = binding.spinnerStatusFilter.selectedItem.toString()
        val todayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val currentTimeMillis = System.currentTimeMillis()
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val updatedTasks = mutableListOf<Task>()

        originalTaskList.forEach { task ->
            if (!task.isCompleted && !task.isDeleted) {
                val fullDue = "${task.dueDate} ${task.dueTime}"
                try {
                    val dueMillis = dateTimeFormat.parse(fullDue)?.time
                    if (dueMillis != null && dueMillis < currentTimeMillis) {
                        val updatedTask = task.copy(isCompleted = true)
                        updatedTasks.add(updatedTask)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (updatedTasks.isNotEmpty()) {
            updatedTasks.forEach { viewModel.updateTask(it) }

            viewModel.loadAllTasks()
            return
        }

        lifecycleScope.launchWhenStarted {
            val filteredMain = originalTaskList.filter { task ->
                val matchesSearch =
                    task.name.contains(query, true) || task.description.contains(query, true)
                val matchesDate = when (taskFilter) {
                    "Today Tasks" -> task.dueDate == todayDate
                    else -> true
                }
                val isActive = !task.isCompleted && !task.isDeleted
                matchesSearch && matchesDate && isActive
            }

            val filteredSecondary = originalTaskList.filter { task ->
                val matchesSearch =
                    task.name.contains(query, true) || task.description.contains(query, true)
                val matchesStatus = when (statusFilter) {
                    "Completed Tasks" -> task.isCompleted && !task.isDeleted
                    "Deleted Tasks" -> task.isDeleted
                    else -> false
                }
                matchesSearch && matchesStatus
            }

            taskAdapter.updateTasks(filteredMain)
            completedDeletedAdapter.updateTasks(filteredSecondary)

            val isAllEmpty = filteredMain.isEmpty() && filteredSecondary.isEmpty()
            if (isAllEmpty) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.spinnerTaskFilterCard.visibility = View.GONE
                binding.spinnerStatusFilterCard.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.spinnerTaskFilterCard.visibility = View.VISIBLE
                binding.spinnerStatusFilterCard.visibility = View.VISIBLE
            }

        }
    }

}