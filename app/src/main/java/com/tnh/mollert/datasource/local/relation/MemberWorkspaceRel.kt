package com.tnh.mollert.datasource.local.relation

import androidx.room.Entity

@Entity(primaryKeys = ["email", "workspaceId"])
data class MemberWorkspaceRel(
    val email: String,
    val workspaceId: String,
    val role: String = ROLE_LEADER
) {
    companion object{
        const val ROLE_LEADER = "leader"
        const val ROLE_MEMBER = "member"
    }
}