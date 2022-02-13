package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Work(
    @PrimaryKey
    val workId: String,
    var workName: String,
    val cardId: String
) {
}