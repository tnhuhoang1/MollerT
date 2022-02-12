package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Board(
    @PrimaryKey
    val boardId: String,
    val boardName: String,
    val workspaceId: String,
    val boardDesc: String? = null,
    val background: String? = null,
    val status: String = STATUS_OPEN,
) {
    companion object{
        const val STATUS_OPEN = "open"
        const val STATUS_CLOSED = "closed"
    }
}