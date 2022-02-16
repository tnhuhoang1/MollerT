package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface MemberWorkspaceDao: BaseDao<MemberWorkspaceRel> {
}