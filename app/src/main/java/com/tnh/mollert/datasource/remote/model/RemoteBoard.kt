package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Board

data class RemoteBoard(
    val boardId: String? = null,
    val workspaceId: String? = null,
    val boardName: String? = null,
    val boardDesc: String? = null,
    var boardBackground: String? = null,
    val boardStatus: String? = Board.STATUS_OPEN,
    var boardVisibility: String = Board.VISIBILITY_PUBLIC,
    val members: List<RemoteMemberRef>? = listOf(),
    val positions: List<RemoteListPositionRef>? = listOf()
): RemoteModel {
    fun toModel(): Board?{
        return convertTo {
            Board(
                boardId!!,
                boardName!!,
                workspaceId!!,
                boardDesc,
                boardBackground,
                boardStatus!!,
                boardVisibility
            )
        }
    }

}