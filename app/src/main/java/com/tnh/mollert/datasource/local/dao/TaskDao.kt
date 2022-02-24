package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.Task
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao: BaseDao<Task> {
    @Query("select * from task where workId = :workId")
    fun getTasksByWorkId(workId: String): Flow<List<Task>>

    @Query("select * from task where workId = :workId")
    suspend fun getTasksByWorkIdNoFlow(workId: String): List<Task>

    @Query("select * from task where taskId = :taskId order by taskName")
    suspend fun getTaskByTaskId(taskId: String): Task?
}