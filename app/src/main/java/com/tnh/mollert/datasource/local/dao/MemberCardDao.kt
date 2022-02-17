package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import com.tnh.mollert.datasource.local.relation.MemberCardRel
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface MemberCardDao: BaseDao<MemberCardRel> {
}