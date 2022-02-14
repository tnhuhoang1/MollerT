package com.tnh.mollert.datasource.remote.model

data class RemoteTask(
    val taskId: String,
    val name: String,
    val dueDate: Long,
    val checked: Boolean,
    val member: RemoteMemberRef,
    val ref: String
): RemoteModel {
}