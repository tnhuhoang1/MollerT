package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.compound.MemberAndBoard
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao: BaseDao<Board> {

    @Query("select count(boardId) from board limit 1")
    suspend fun countOne(): Int?

    @Query("select count(boardId) from board limit 1")
    fun countOneFlow(): Flow<Int>

    @Query("select * from board where boardId = :boardId")
    fun getBoardById(boardId: String): Flow<Board>

    @Query("select * from board where boardId = :boardId")
    suspend fun getBoardByIdNoFlow(boardId: String): Board?

    @Query("select * from board where boardName like :search and boardStatus = 'open'")
    suspend fun searchBoard(search: String): List<Board>

    @Query("select * from member as m, board as b, workspace as w, memberboardrel as mb " +
            "where b.workspaceId = w.workspaceId and mb.boardId = b.boardId and mb.email = m.email and m.email = :email")
    fun getAllBoardByEmail(email: String): Flow<List<MemberAndBoard>>
}