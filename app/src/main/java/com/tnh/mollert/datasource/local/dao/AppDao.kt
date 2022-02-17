package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.tnh.mollert.datasource.local.compound.MemberWithWorkspaces
import com.tnh.mollert.datasource.local.compound.WorkspaceWithBoards
import com.tnh.mollert.datasource.local.compound.WorkspaceWithMembers
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Transaction
    @Query("select * from member where email = :email")
    fun getMemberWithWorkspaces(email: String): Flow<MemberWithWorkspaces>

    @Transaction
    @Query("select * from member where email = :email")
    suspend fun getMemberWithWorkspacesNoFlow(email: String): MemberWithWorkspaces?

    @Transaction
    @Query("select * from workspace where workspaceId = :workspaceId")
    suspend fun getWorkspaceWithMembersNoFlow(workspaceId: String): WorkspaceWithMembers?

    @Transaction
    @Query("select * from workspace where workspaceId = :id")
    fun getWorkspaceWithBoards(id: String): Flow<WorkspaceWithBoards>

    @Transaction
    @Query("select * from workspace where workspaceId = :id")
    suspend fun getWorkspaceWithBoardsNoFlow(id: String): WorkspaceWithBoards
}