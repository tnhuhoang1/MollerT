package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface MemberBoardDao: BaseDao<MemberBoardRel> {
    @Query("select * from memberboardrel where email = :email and boardId = :boardId")
    suspend fun getRelByEmailAndBoardId(email: String, boardId: String): MemberBoardRel?

    @Query("select * from memberboardrel where boardId = :boardId")
    suspend fun getRelsByBoardId(boardId: String): List<MemberBoardRel>
}