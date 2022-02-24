package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.mollert.datasource.local.relation.MemberCardRel
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface MemberCardDao: BaseDao<MemberCardRel> {
    @Query("select * from membercardrel where cardId = :cardId")
    suspend fun getRelByCardId(cardId: String): List<MemberCardRel>

    @Query("select * from membercardrel where cardId = :cardId and email = :email")
    suspend fun getRelByEmailAndCardId(email: String, cardId: String): MemberCardRel?
}