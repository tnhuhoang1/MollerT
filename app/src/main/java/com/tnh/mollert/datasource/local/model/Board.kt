package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Board(
    @PrimaryKey
    val boardId: String,
    var boardName: String,
    val workspaceId: String,
    var boardDesc: String? = null,
    var background: String? = null,
    var status: String = STATUS_OPEN,
    var boardVisibility: String = VISIBILITY_PUBLIC
) {
    companion object{
        const val STATUS_OPEN = "open"
        const val STATUS_CLOSED = "closed"
        const val VISIBILITY_WORKSPACE = "workspace"
        const val VISIBILITY_PRIVATE = "private"
        const val VISIBILITY_PUBLIC = "public"
    }
}