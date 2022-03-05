package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface CardLabelDao: BaseDao<CardLabelRel> {
    @Query("select * from cardlabelrel where cardId = :cardId")
    suspend fun getRelByCardId(cardId: String): List<CardLabelRel>

    @Query("select * from cardlabelrel where labelId = :labelId")
    suspend fun getRelByLabelId(labelId: String): List<CardLabelRel>
}