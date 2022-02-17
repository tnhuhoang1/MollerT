package com.tnh.mollert.datasource.local.relation

import androidx.room.Entity

@Entity(primaryKeys = ["email", "boardId"])
class MemberBoardRel(
    val email: String,
    val boardId: String,
    var role: String = ROLE_MEMBER
) {
    companion object{
        const val ROLE_MEMBER = "member"
    }
}