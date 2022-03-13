package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.compound.BoardAndCard
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao: BaseDao<Card> {

    @Query("select * from card where listIdPar = :listId and cardStatus = :status order by cardName asc")
    fun getCardsWithListId(listId: String, status: String = Card.STATUS_ACTIVE): Flow<List<Card>>

    @Query("select * from card where listIdPar = :listId and cardStatus = :status order by cardName asc")
    fun getCardsWithListIdSortedByName(listId: String, status: String = Card.STATUS_ACTIVE): Flow<List<Card>>

    @Query("select * from card where listIdPar = :listId and cardStatus = :status order by createdAt desc")
    fun getCardsWithListIdSortedByDateAdded(listId: String, status: String = Card.STATUS_ACTIVE): Flow<List<Card>>

    @Query("select * from card where listIdPar = :listId and cardStatus = :status order by dueDate asc")
    fun getCardsWithListIdSortedByDueDate(listId: String, status: String = Card.STATUS_ACTIVE): Flow<List<Card>>

    @Query("select * from card as c, list as l where l.listId = c.listIdPar and l.boardId = :boardId order by dueDate asc")
    suspend fun getCardsInBoardSortedByDueDate(boardId: String): List<Card>

    @Query("select * from card, list where card.listIdPar = list.listId and list.boardId = :boardId and card.cardStatus = :status")
    fun getCardsWithBoardId(boardId: String, status: String = Card.STATUS_ACTIVE): Flow<List<Card>>

    @Query("select * from card where cardId = :cardId")
    fun getCardById(cardId: String): Flow<Card>

    @Query("select * from card where cardId = :cardId")
    suspend fun getCardByIdNoFlow(cardId: String): Card?

    @Query("select * from card as c, board as b, list as l where c.listIdPar = l.listId and l.boardId = b.boardId and c.cardId = :cardId")
    suspend fun getBoardAndCardByCardId(cardId: String): BoardAndCard?

    @Query("select * from card where listIdPar = :listId and cardStatus = 'active'")
    suspend fun getActiveCardsByListId(listId: String): List<Card>

    @Query("select * from list as l, card as c, board as b, member as m, memberboardrel as mb" +
            " where m.email = mb.email and mb.boardId = b.boardId and b.boardId = l.boardId and l.listId = c.listIdPar and c.startDate > 0 and c.dueDate > 0 and c.checked = 0 and c.cardStatus = 'active' and b.boardStatus = 'open' and m.email = :email " +
            " order by c.startDate asc"
    )
    fun getCardHasDateFlow(email: String): Flow<List<Card>>

    @Query("select * from card as c, list as l where c.cardName like :search " +
            "and c.listIdPar = l.listId and l.boardId = :boardId and c.cardStatus = 'active'"
    )
    suspend fun searchCardInBoard(search: String, boardId: String): List<Card>
}