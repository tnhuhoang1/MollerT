package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import com.tnh.mollert.datasource.local.model.Work
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface WorkDao: BaseDao<Work> {
}