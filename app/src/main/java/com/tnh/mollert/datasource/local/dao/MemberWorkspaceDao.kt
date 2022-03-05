package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface MemberWorkspaceDao: BaseDao<MemberWorkspaceRel> {
    @Query("select * from memberworkspacerel as mw, workspace as w where w.workspaceId = mw.workspaceId and (mw.role = 'leader' or mw.role = 'owner') and w.workspaceId = :workspaceId")
    suspend fun getWorkspaceLeader(workspaceId: String): MemberWorkspaceRel?
}