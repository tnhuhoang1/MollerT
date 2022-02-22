package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface CardLabelDao: BaseDao<CardLabelRel> {

    @Query("select * from cardlabelrel where cardId = :cardId")
    suspend fun getAllByCardId(cardId: String): List<CardLabelRel>
}