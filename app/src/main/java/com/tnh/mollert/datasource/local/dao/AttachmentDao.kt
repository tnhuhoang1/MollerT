package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tnh.mollert.datasource.local.model.Attachment
import com.tnh.tnhlibrary.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao: BaseDao<Attachment> {
    @Query("select * from attachment where cardId = :cardId")
    fun getAllByCardId(cardId: String): Flow<List<Attachment>>

    @Query("select * from attachment where cardId = :cardId")
    suspend fun getAllByCardIdNoFlow(cardId: String): List<Attachment>


    @Query("select * from attachment where attachmentId = :attachmentId")
    suspend fun getAttachmentById(attachmentId: String): Attachment?
}