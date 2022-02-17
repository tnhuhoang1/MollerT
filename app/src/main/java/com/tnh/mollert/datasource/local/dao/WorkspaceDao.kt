package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao: BaseDao<Workspace> {

    @Query("select * from workspace")
    fun getAll(): Flow<List<Workspace>>

    /**
     *
     */
    @Query("select count(workspaceId) from workspace limit 1")
    suspend fun countOne(): Int?
}