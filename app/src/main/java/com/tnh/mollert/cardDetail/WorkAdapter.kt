package com.tnh.mollert.cardDetail

import com.tnh.mollert.R
import com.tnh.mollert.databinding.WorkItemBinding
import com.tnh.mollert.datasource.local.dao.TaskDao
import com.tnh.mollert.datasource.local.model.Task
import com.tnh.mollert.datasource.local.model.Work
import com.tnh.tnhlibrary.dataBinding.recycler.DataBindingViewHolder
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleDataBindingListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class WorkAdapter(private val taskDao: TaskDao): SimpleDataBindingListAdapter<Work, WorkItemBinding>(R.layout.work_item) {

    var onAddItemClicked: (workId: String)-> Unit = {}
    var onDeleteWorkClicked: (work: Work)-> Unit = {}
    var onDeleteTaskClicked: (task: Task) -> Unit = {}
    var onTaskChecked: (task: Task, isChecked: Boolean) -> Unit = {_, _ ->}

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onBindViewHolder(holder: DataBindingViewHolder<WorkItemBinding>, position: Int) {
        val item = getItem(position)
        val taskAdapter = TaskAdapter(taskDao)
        holder.binding.apply {
            workItemTitle.text = item.workName
            workItemDeleteWork.setOnClickListener {
                onDeleteWorkClicked(item)
            }
            workItemAddItem.setOnClickListener {
                onAddItemClicked(item.workId)
            }
            workItemRecycler.adapter = taskAdapter
            taskAdapter.onTaskDeleteClicked = onDeleteTaskClicked
            taskAdapter.onCheckedChanged = onTaskChecked
            taskAdapter.scope = scope
            taskAdapter.fetchList(item.workId)
        }
    }
}