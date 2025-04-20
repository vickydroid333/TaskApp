package com.example.taskapp

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskapp.databinding.ItemTaskBinding

class TaskAdapter(
    private var taskList: List<Task>,
    private val listener: OnTaskClickListener,
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(Color.WHITE, Color.GRAY)
        )
        holder.binding.radioCompleteTask.buttonTintList = colorStateList
        holder.binding.radioCompleteTask.setOnCheckedChangeListener { _, isChecked ->
            holder.binding.radioCompleteTask.setTextColor(if (isChecked) Color.BLUE else Color.GRAY)
        }

        holder.binding.apply {
            textViewTaskName.text = task.name
            textViewDueDate.text = "Due Date: ${task.dueDate} ${task.dueTime}"
            textViewPriority.text = "Priority: ${task.priority}"

            root.setOnClickListener {
                listener.onTaskClick(task.id)
            }

            radioCompleteTask.isChecked = task.isCompleted

            radioCompleteTask.setOnClickListener {
                if (task.isCompleted) {
                    listener.onTaskIncompleteConfirm(task)
                    radioCompleteTask.isChecked = true
                } else {
                    listener.onTaskCompleteConfirm(task)
                    radioCompleteTask.isChecked = false
                }
            }
        }
    }

    override fun getItemCount(): Int = taskList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateTasks(newTasks: List<Task>) {
        this.taskList = newTasks
        notifyDataSetChanged()
    }
}

interface OnTaskClickListener {
    fun onTaskClick(taskId: Int)
    fun onTaskCompleteConfirm(task: Task)
    fun onTaskIncompleteConfirm(task: Task)

}
