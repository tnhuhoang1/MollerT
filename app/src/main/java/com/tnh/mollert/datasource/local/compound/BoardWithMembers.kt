package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.local.relation.MemberBoardRel

data class BoardWithMembers(
    @Embedded
    val board: Board,
    @Relation(
        parentColumn = "boardId",
        entityColumn = "email",
        associateBy = Junction(MemberBoardRel::class)
    )
    val members: List<Member>
) {
}