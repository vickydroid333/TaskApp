package com.example.taskapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskapp.databinding.ActivityTaskDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailsBinding
    private val viewModel: AddEditTaskViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskId = intent.getIntExtra("taskId", -1)
        if (taskId == -1) {
            Toast.makeText(this, "Invalid Task", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Observe StateFlow
        lifecycleScope.launchWhenStarted {
            viewModel.task.collect { task ->
                task?.let {
                    binding.textDetailName.text = it.name
                    binding.textDetailDesc.text = it.description
                    binding.textDetailCategory.text = "Category: ${it.category}"
                    binding.textDetailPriority.text = "Priority: ${it.priority}"
                    binding.textDetailDate.text = "Date: ${it.dueDate} Time: ${it.dueTime}"
                }
            }
        }

        viewModel.loadTaskById(taskId)

        binding.buttonEdit.setOnClickListener {
            val intent = Intent(this, AddEditTaskActivity::class.java).apply {
                putExtra("isEdit", true)
                putExtra("taskId", taskId)
            }
            startActivity(intent)
            finish()
        }

        binding.buttonDelete.setOnClickListener {
            val currentTask = viewModel.task.value
            AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete") { _, _ ->
                    if (currentTask != null) {
                        viewModel.deleteTask(currentTask)
                        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Unable to delete. Task not loaded.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()

        }
    }
}
