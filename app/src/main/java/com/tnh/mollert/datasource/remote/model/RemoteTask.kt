package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Task

data class RemoteTask(
    val taskId: String? = null,
    val name: String? = null,
    val workId: String? = null,
    val dueDate: Long = 0L,
    val checked: Boolean = false,
    val email: String = "",
    val ref: String? = null
): RemoteModel {
    fun toModel(): Task?{
        return convertTo {
            Task(
                taskId!!,
                name!!,
                workId!!,
                email,
                dueDate,
                checked
            )
        }
    }
}