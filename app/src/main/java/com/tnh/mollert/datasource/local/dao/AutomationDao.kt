package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import com.tnh.mollert.datasource.local.model.Automation
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface AutomationDao: BaseDao<Automation> {
}