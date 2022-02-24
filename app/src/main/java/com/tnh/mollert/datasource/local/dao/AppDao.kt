package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.tnh.mollert.datasource.local.compound.*
import kotlinx.coroutines.flow.Flow
import java.lang.StringBuilder

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

    @Query("select * from member, activity where member.email = activity.actor and member.email = :email")
    fun getActivityAssocWithEmail(email: String): Flow<List<MemberAndActivity>>

    @Query("select * from member, activity where member.email = activity.actor and activity.cardId = :cardId")
    fun getMemberAndActivityByCardIdFlow(cardId: String): Flow<List<MemberAndActivity>>

    @Query("select * from member, activity where member.email = activity.actor and activity.boardId = :boardId")
    fun getMemberAndActivityByBoardIdFlow(boardId: String): Flow<List<MemberAndActivity>>

    @Query("select * from card where cardId = :cardId")
    fun getCardWithMembersByCardIdFlow(cardId: String): Flow<CardWithMembers>

    @Transaction
    @Query("select * from board where boardId = :boardId")
    suspend fun getBoardWithMembers(boardId: String): BoardWithMembers?

    @Transaction
    @Query("select * from board where boardId = :boardId")
    fun getBoardWithLists(boardId: String): Flow<BoardWithLists>

    @Transaction
    @Query("select * from card where cardId = :cardId")
    suspend fun getCardWithLabels(cardId: String): CardWithLabels

    @Transaction
    @Query("select * from card where cardId = :cardId")
    fun getCardWithLabelsFlow(cardId: String): Flow<CardWithLabels>
}