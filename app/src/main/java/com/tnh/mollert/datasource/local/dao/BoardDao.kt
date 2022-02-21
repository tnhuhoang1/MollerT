package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
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
}