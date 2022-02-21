package com.tnh.mollert.datasource.local.relation

import androidx.room.Entity
import com.tnh.mollert.datasource.remote.model.RemoteMemberRef

@Entity(primaryKeys = ["email", "boardId"])
class MemberBoardRel(
    val email: String,
    val boardId: String,
    var role: String = ROLE_MEMBER
) {
    companion object{
        const val ROLE_MEMBER = RemoteMemberRef.ROLE_MEMBER
        const val ROLE_OWNER = RemoteMemberRef.ROLE_OWNER
    }
}