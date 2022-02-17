package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface CardLabelDao: BaseDao<CardLabelRel> {
}