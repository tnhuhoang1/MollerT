package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import androidx.room.Relation
import com.tnh.mollert.datasource.local.model.Board

data class BoardWithLists(
    @Embedded
    val board: Board,
    @Relation(
        parentColumn = "boardId",
        entityColumn = "boardId"
    )
    val lists: List<com.tnh.mollert.datasource.local.model.List>
) {
}