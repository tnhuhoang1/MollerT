package com.tnh.mollert.datasource.remote.model

data class RemoteActivity(
    val activityId: String? = null,
    val message: String? = null,
    val timestamp: Long? = null
): RemoteModel {
}