package com.tnh.mollert.datasource.remote.model

data class RemoteBoard(
    val boardId: String,
    val boardName: String,
    val boardDesc: String?,
    val members: List<RemoteMemberRef>,
    val positions: List<RemoteListPositionRef>
) {
}