package com.tnh.mollert.datasource.local.relation

import androidx.room.Entity

@Entity(primaryKeys = ["email", "workspaceId"])
data class MemberWorkspaceRel(
    val email: String,
    val workspaceId: String
) {
}