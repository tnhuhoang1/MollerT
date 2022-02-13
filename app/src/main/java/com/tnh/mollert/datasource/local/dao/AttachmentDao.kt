package com.tnh.mollert.datasource.local.dao

import androidx.room.Dao
import com.tnh.mollert.datasource.local.model.Attachment
import com.tnh.tnhlibrary.room.BaseDao

@Dao
interface AttachmentDao: BaseDao<Attachment> {
}