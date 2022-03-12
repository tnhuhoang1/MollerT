package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class List(
    @PrimaryKey
    val listId: String,
    var listName: String,
    val boardId: String,
    var status: String,
    var position: Int
) {
    companion object{
        const val STATUS_ACTIVE = "active"
        const val STATUS_ARCHIVED = "archived"
    }
}