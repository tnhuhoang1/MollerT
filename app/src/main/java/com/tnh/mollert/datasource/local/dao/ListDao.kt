package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import com.tnh.mollert.datasource.local.model.List
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface ListDao: BaseDao<List> {
}