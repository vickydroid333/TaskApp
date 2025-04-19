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

    private val viewModel: AddEditTaskViewModel by viewModels()
    private lateinit var binding: ActivityAddEditTaskBinding

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

        val isEditMode = intent.getBooleanExtra("isEdit", false)
        val taskId = intent.getIntExtra("taskId", -1)

        if (isEditMode && taskId != -1) {
            viewModel.loadTaskById(taskId)

            lifecycleScope.launch {
                viewModel.task.collect { task ->
                    task?.let {
                        binding.editTextTaskName.setText(it.name)
                        binding.editTextDescription.setText(it.description)
                        binding.textViewPickDate.text = it.dueDate
                        binding.textViewPickTime.text = it.dueTime
                        binding.seekBarPriority.progress = it.priority
                        binding.textPriorityValue.text = "Priority: ${it.priority}"

                        val categoryIndex = categoryList.indexOf(it.category)
                        if (categoryIndex != -1) {
                            binding.spinnerCategory.setSelection(categoryIndex)
                        }

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
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoryList
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = spinnerAdapter

        binding.seekBarPriority.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                binding.textPriorityValue.text = "Priority: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Date Picker
        binding.textViewPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val date = "${dayOfMonth.toString().padStart(2, '0')}/${
                        (month + 1).toString().padStart(2, '0')
                    }/$year"
                    binding.textViewPickDate.text = date
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Time Picker
        binding.textViewPickTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePicker = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val hour = hourOfDay.toString().padStart(2, '0')
                    val min = minute.toString().padStart(2, '0')
                    val time = "$hour:$min"
                    binding.textViewPickTime.text = time
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }


        binding.buttonSave.setOnClickListener {
            if (isValidInput()) {
                viewModel.taskName.value = binding.editTextTaskName.text.toString()
                viewModel.taskDescription.value = binding.editTextDescription.text.toString()
                viewModel.taskDate.value = binding.textViewPickDate.text.toString()
                viewModel.taskTime.value = binding.textViewPickTime.text.toString()
                viewModel.taskCategory.value = binding.spinnerCategory.selectedItem.toString()
                viewModel.taskPriority.value = binding.seekBarPriority.progress

                viewModel.insertTask()
                Toast.makeText(this, "Task Saved", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun isValidInput(): Boolean {
        return binding.editTextTaskName.text.isNotEmpty() &&
                binding.textViewPickDate.text.isNotEmpty() &&
                binding.textViewPickTime.text.isNotEmpty()
    }

}
