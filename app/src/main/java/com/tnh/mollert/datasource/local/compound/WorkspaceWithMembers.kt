package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel

data class WorkspaceWithMembers(
    @Embedded
    val workspace: Workspace,
    @Relation(
        parentColumn = "workspaceId",
        entityColumn = "email",
        associateBy = Junction(MemberWorkspaceRel::class)
    )
    val members: List<Member>
) {
}