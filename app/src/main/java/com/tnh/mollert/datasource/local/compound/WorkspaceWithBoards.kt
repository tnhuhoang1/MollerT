package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import androidx.room.Relation
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace

data class WorkspaceWithBoards(
    @Embedded
    val workspace: Workspace,
    @Relation(
        parentColumn = "workspaceId",
        entityColumn = "workspaceId"
    )
    val boards: List<Board>
) {

}