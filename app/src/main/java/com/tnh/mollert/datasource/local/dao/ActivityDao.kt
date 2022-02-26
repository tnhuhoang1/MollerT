package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface ActivityDao: BaseDao<Activity> {
    @Query("select * from activity where activityId = :activityId")
    suspend fun getActivityById(activityId: String): Activity?

    @Query("select * from activity where cardId = :cardId")
    suspend fun getActivityByCardId(cardId: String): List<Activity>

}