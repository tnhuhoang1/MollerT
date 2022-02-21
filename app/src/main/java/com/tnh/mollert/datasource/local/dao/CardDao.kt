package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao: BaseDao<Card> {

    @Query("select * from card where listId = :listId")
    fun getCardsWithListId(listId: String): Flow<List<Card>>
}