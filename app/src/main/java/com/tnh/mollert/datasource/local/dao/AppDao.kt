package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.tnh.mollert.datasource.local.compound.*
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Member
import kotlinx.coroutines.flow.Flow
import java.lang.StringBuilder

@Dao
interface AppDao {
    @Transaction
    @Query("select * from member where email = :email")
    fun getMemberWithWorkspaces(email: String): Flow<MemberWithWorkspaces>

    @Query("select * from member as m, workspace as w, MemberWorkspaceRel as mw " +
            "where w.workspaceId = mw.workspaceId and m.email = mw.email and w.workspaceId = :workspaceId")
    suspend fun getMemberByWorkspaceId(workspaceId: String): List<Member>

    @Transaction
    @Query("select * from member where email = :email")
    suspend fun getMemberWithWorkspacesNoFlow(email: String): MemberWithWorkspaces?

    @Query("select * from member as m, board as b, memberboardrel as mb, activity as a " +
            "where m.email = mb.email and mb.boardId = b.boardId and a.boardId = b.boardId and a.actor = m.email order by timestamp desc")
    fun getAllMemberAndActivityByEmail(): Flow<List<MemberAndActivity>>

    @Transaction
    @Query("select * from workspace where workspaceId = :workspaceId")
    suspend fun getWorkspaceWithMembersNoFlow(workspaceId: String): WorkspaceWithMembers?

    @Transaction
    @Query("select * from workspace where workspaceId = :workspaceId")
    fun getWorkspaceWithMembers(workspaceId: String): Flow<WorkspaceWithMembers>

    @Transaction
    @Query("select * from workspace where workspaceId = :id")
    fun getWorkspaceWithBoards(id: String): Flow<WorkspaceWithBoards>

    @Transaction
    @Query("select * from workspace where workspaceId = :id")
    suspend fun getWorkspaceWithBoardsNoFlow(id: String): WorkspaceWithBoards

    @Query("select * from member, activity where member.email = activity.actor and member.email = :email order by timestamp desc")
    fun getActivityAssocWithEmail(email: String): Flow<List<MemberAndActivity>>

    @Query("select * from member, activity where member.email = activity.actor and activity.cardId = :cardId order by activity.timestamp desc")
    fun getMemberAndActivityByCardIdFlow(cardId: String): Flow<List<MemberAndActivity>>

    @Query("select * from member, activity where member.email = activity.actor and activity.boardId = :boardId order by activity.timestamp desc")
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