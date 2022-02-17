package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Board

data class RemoteBoard(
    val boardId: String? = null,
    val boardName: String? = null,
    val boardDesc: String? = null,
    val boardBackground: String? = null,
    val boardStatus: String? = Board.STATUS_OPEN,
    val members: List<RemoteMemberRef>? = null,
    val positions: List<RemoteListPositionRef>? = null
): RemoteModel {
    fun toModel(workspaceId: String): Board?{
        return convertTo {
            Board(
                boardId!!,
                boardName!!,
                workspaceId,
                boardDesc,
                boardBackground,
                boardStatus!!
            )
        }
    }
}