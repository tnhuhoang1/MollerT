package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel

data class MemberWithWorkspaces(
    @Embedded
    val member: Member,
    @Relation(
        parentColumn = "email",
        entityColumn = "workspaceId",
        associateBy = Junction(MemberWorkspaceRel::class)
    )
    val workspaces: List<Workspace>
) {
}