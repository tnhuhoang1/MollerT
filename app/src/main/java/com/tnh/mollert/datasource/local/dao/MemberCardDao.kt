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

    @Query("select * from membercardrel as mc, list as l, card as c where mc.cardId = c.cardId and c.listIdPar = l.listId and l.boardId = :boardId and mc.email = :email order by mc.email asc")
    suspend fun getRelByEmailInBoard(email: String, boardId: String): List<MemberCardRel>

    @Query("select * from membercardrel where cardId = :cardId and email = :email")
    suspend fun getRelByEmailAndCardId(email: String, cardId: String): MemberCardRel?
}