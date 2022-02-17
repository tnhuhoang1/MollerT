package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface MemberBoardDao: BaseDao<MemberBoardRel> {
}