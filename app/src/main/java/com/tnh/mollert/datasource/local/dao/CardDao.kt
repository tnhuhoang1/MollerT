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
}