package com.tnh.mollert.datasource.remote.model

data class RemoteActivity(
    val activityId: String,
    val message: String,
    val timestamp: Long
): RemoteModel {
}