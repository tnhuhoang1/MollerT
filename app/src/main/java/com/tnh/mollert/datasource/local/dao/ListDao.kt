package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.List
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface ListDao: BaseDao<List> {
    @Query("select * from list where listId = :listId")
    suspend fun getListByListId(listId: String): List?
}