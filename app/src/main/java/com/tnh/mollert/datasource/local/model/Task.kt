package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey
    val taskId: String,
    var taskName: String,
    val workId: String,
    var email: String? = null,
    var dueDate: Long = 0L,
    var checked: Boolean = false
) {
}