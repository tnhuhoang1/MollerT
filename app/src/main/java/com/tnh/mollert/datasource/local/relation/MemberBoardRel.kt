package com.tnh.mollert.datasource.local.relation

import androidx.room.Entity

@Entity(primaryKeys = ["email", "boardId"])
class MemberBoardRel(
    val email: String,
    val boardId: String,
    var role: String
) {

}