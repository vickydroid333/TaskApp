package com.example.taskapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnTaskClickListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AddEditTaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    private val addEditTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.loadAllTasks()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        lifecycleScope.launchWhenStarted {
            viewModel.allTasks.collectLatest { tasks ->
                adapter.updateTasks(tasks)
            }
        }

        viewModel.loadAllTasks()

        binding.fabAddTask.setOnClickListener {
            val intent = Intent(this, AddEditTaskActivity::class.java)
            addEditTaskLauncher.launch(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(emptyList(), this)
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTasks.adapter = adapter
    }

    override fun onTaskClick(taskId: Int) {
        val intent = Intent(this, TaskDetailActivity::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("isEdit", true)
        }
        addEditTaskLauncher.launch(intent)
    }
}