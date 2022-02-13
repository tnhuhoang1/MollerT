package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface WorkspaceDao: BaseDao<Workspace> {
}