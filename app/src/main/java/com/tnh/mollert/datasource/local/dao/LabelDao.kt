package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.Label
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao: BaseDao<Label> {

    @Query("select * from label")
    fun getAll(): Flow<List<Label>>

    @Query("select count(labelId) from label limit 1")
    suspend fun isEmpty(): Int

    @Query("select * from label where boardId = :boardId")
    fun getLabelsWithBoardId(boardId: String): Flow<List<Label>>

    @Query("select * from label where boardId = :boardId")
    suspend fun getLabelsWithBoardIdNoFlow(boardId: String): List<Label>

    @Query("select * from label where labelId = :labelId")
    suspend fun getLabelById(labelId: String): Label?
}