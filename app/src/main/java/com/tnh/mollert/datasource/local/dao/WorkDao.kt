package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.Work
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkDao: BaseDao<Work> {
    @Query("select * from work where cardId = :cardId")
    fun getWorksByCardIdFlow(cardId: String): Flow<List<Work>>

    @Query("select * from work where cardId = :cardId")
    suspend fun getWorksByCardId(cardId: String): List<Work>

    @Query("select * from work where workId = :workId")
    suspend fun getWorkById(workId: String): Work?
}