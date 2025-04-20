package com.example.taskapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskapp.databinding.ActivityAddEditTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTaskBinding
    private val viewModel: AddEditTaskViewModel by viewModels()
    private val categoryList = listOf(
        "Grocery",
        "work",
        "Sport",
        "Design",
        "University",
        "Social",
        "Music",
        "Health",
        "Movie",
        "Home"
    )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isEdit = intent.getBooleanExtra("isEdit", false)
        val taskId = intent.getIntExtra("taskId", -1)

        setupUI()
        setupCategorySpinner()

        if (isEdit && taskId != -1) {
            viewModel.loadTaskById(taskId)

            lifecycleScope.launch {
                viewModel.task.collect { task ->
                    task?.let {
                        binding.editTextTaskName.setText(it.name)
                        binding.editTextDescription.setText(it.description)
                        binding.textViewPickDate.text = it.dueDate
                        binding.textViewPickTime.text = it.dueTime
                        binding.seekBarPriority.progress = it.priority

                        viewModel.taskName.value = it.name
                        viewModel.taskDescription.value = it.description
                        viewModel.taskDate.value = it.dueDate
                        viewModel.taskTime.value = it.dueTime
                        viewModel.taskCategory.value = it.category
                        viewModel.taskPriority.value = it.priority
                        viewModel.editingTaskId.value = it.id
                    }
                }
            }
        }

        binding.buttonSave.setOnClickListener {
            if (isValid()) {
                updateViewModelValues()
                viewModel.insertTask()

                Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        binding.seekBarPriority.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textPriorityValue.text = "Priority: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.textViewPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    binding.textViewPickDate.text = "${"%02d".format(d)}/${"%02d".format(m + 1)}/$y"
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.textViewPickTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, h, m ->
                    binding.textViewPickTime.text = "${"%02d".format(h)}:${"%02d".format(m)}"
                },
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show()
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun isValid(): Boolean {
        return binding.editTextTaskName.text.isNotBlank()
                && binding.textViewPickDate.text.isNotBlank()
                && binding.textViewPickTime.text.isNotBlank()
    }

    private fun updateViewModelValues() {
        viewModel.taskName.value = binding.editTextTaskName.text.toString()
        viewModel.taskDescription.value = binding.editTextDescription.text.toString()
        viewModel.taskDate.value = binding.textViewPickDate.text.toString()
        viewModel.taskTime.value = binding.textViewPickTime.text.toString()
        viewModel.taskCategory.value = binding.spinnerCategory.selectedItem.toString()
        viewModel.taskPriority.value = binding.seekBarPriority.progress
    }
}
