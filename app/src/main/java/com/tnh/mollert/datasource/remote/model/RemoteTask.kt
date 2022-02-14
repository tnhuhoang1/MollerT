package com.tnh.mollert.datasource.remote.model

data class RemoteTask(
    val taskId: String? = null,
    val name: String? = null,
    val dueDate: Long? = null,
    val checked: Boolean? = null,
    val member: RemoteMemberRef? = null,
    val ref: String? = null
): RemoteModel {
}