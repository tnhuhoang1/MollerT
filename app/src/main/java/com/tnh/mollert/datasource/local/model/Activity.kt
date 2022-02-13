package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Activity(
    @PrimaryKey
    val activityId: String,
    val actor: String,
    val boardId: String? = null,
    val cardId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
}