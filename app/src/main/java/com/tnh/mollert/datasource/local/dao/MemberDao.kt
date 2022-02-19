package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao: BaseDao<Member> {
    @Query("select * from member")
    fun getAll(): Flow<List<Member>>

    @Query("select * from member where email = :email")
    suspend fun getByEmail(email: String): Member?

    @Query("select * from member where email = :email")
    fun getByEmailFlow(email: String): Flow<Member?>
}