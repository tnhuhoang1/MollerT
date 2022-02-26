package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao: BaseDao<Card> {

    @Query("select * from card where listId = :listId and status = :status")
    fun getCardsWithListId(listId: String, status: String = Card.STATUS_ACTIVE): Flow<List<Card>>

    @Query("select * from card, list where card.listId = list.listId and list.boardId = :boardId and card.status = :status")
    fun getCardsWithBoardId(boardId: String, status: String = Card.STATUS_ACTIVE): Flow<List<Card>>

    @Query("select * from card where cardId = :cardId")
    fun getCardById(cardId: String): Flow<Card>

    @Query("select * from card where cardId = :cardId")
    suspend fun getCardByIdNoFlow(cardId: String): Card?

    @Query("select * from list as l, card as c, board as b, member as m, memberboardrel as mb" +
            " where m.email = mb.email and mb.boardId = b.boardId and b.boardId = l.boardId and l.listId = c.listId and c.startDate > 0 and c.dueDate > 0 and c.checked = 0 and c.status = 'active' and b.status = 'open' and m.email = :email and c.startDate > :date" +
            " order by c.startDate asc"
    )
    fun getCardHasDateFlow(email: String, date: Long = System.currentTimeMillis()): Flow<List<Card>>
}