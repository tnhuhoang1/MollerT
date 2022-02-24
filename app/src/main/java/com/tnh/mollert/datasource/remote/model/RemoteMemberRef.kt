package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.relation.MemberBoardRel

data class RemoteMemberRef(
    val email: String? = null,
    val ref: String? = null,
    val role: String = ROLE_OWNER
): RemoteModel {

    fun toMemberBoardReal(boardId: String?): MemberBoardRel?{
        return convertTo {
            MemberBoardRel(
                email!!,
                boardId!!,
                role
            )
        }
    }

    companion object{
        const val ROLE_OWNER = "owner"
        const val ROLE_MEMBER = "member"
        const val ROLE_LEADER = "leader"
        const val ROLE_CARD_CREATOR = "creator"
        const val ROLE_CARD_WORKER = "worker"
    }
}