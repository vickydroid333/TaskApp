package com.example.taskapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskapp.databinding.ActivityAddEditTaskBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
