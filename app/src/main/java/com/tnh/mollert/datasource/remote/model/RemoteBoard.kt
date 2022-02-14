package com.tnh.mollert.datasource.remote.model

data class RemoteBoard(
    val boardId: String? = null,
    val boardName: String? = null,
    val boardDesc: String? = null,
    val members: List<RemoteMemberRef>? = null,
    val positions: List<RemoteListPositionRef>? = null
): RemoteModel {
}