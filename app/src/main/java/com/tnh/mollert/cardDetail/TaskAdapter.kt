package com.tnh.mollert.cardDetail

import android.text.Spannable
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import com.tnh.mollert.R
import com.tnh.mollert.databinding.TaskItemBinding
import com.tnh.mollert.databinding.WorkItemBinding
import com.tnh.mollert.datasource.local.dao.TaskDao
import com.tnh.mollert.datasource.local.model.Task
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter
import com.tnh.tnhlibrary.logAny
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

class TaskAdapter(private val taskDao: TaskDao): SimpleDataBindingListAdapter<Task, TaskItemBinding>(R.layout.task_item) {
    var scope: CoroutineScope? = null
    private var job: Job? = null
    var onTaskDeleteClicked: (task: Task) -> Unit = {}
    var onCheckedChanged: (task: Task, checked: Boolean) -> Unit = {_,_->}
    fun fetchList(workId: String, workBinding: WorkItemBinding){
        job?.cancel()
        job = scope?.launch {
            taskDao.getTasksByWorkId(workId).collectLatest { listTask->
                withContext(Dispatchers.Main){
                    val progress = ((listTask.count { it.checked }.toFloat() / if(listTask.isEmpty()) 1 else listTask.size) * 100).toInt()
                    workBinding.workItemProgress.progress = progress
                    workBinding.workItemProgressLabel.text = "$progress%"
                    submitList(listTask)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: DataBindingViewHolder<TaskItemBinding>, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            if(item.checked){
                val spannable = SpannableString(item.taskName)
                spannable.setSpan(StrikethroughSpan(), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                taskItemName.text = spannable
            }else{
                taskItemName.text = item.taskName
            }
            taskItemName.isChecked = item.checked
            taskItemName.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked != item.checked){
                    onCheckedChanged(item, isChecked)
                }
            }
            taskItemDelete.setOnClickListener {
                onTaskDeleteClicked(item)
            }
        }
    }
}