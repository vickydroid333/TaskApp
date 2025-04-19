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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: AddEditTaskViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter

    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Reload tasks when returning from AddEditTaskActivity
        viewModel.loadAllTasks()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TaskAdapter(emptyList())
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTasks.adapter = adapter

        // Load tasks initially
        viewModel.loadAllTasks()

        // Collect tasks using Coroutine
        lifecycleScope.launch {
            viewModel.allTasks.collect { tasks ->
                adapter = TaskAdapter(tasks)
                binding.recyclerViewTasks.adapter = adapter
            }
        }

        binding.fabAddTask.setOnClickListener {
            val intent = Intent(this, AddEditTaskActivity::class.java)
            addTaskLauncher.launch(intent)
        }

    }
}