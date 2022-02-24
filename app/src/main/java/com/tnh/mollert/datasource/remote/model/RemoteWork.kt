package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Work

data class RemoteWork(
    val workId: String? = null,
    val name: String? = null,
    val ref: String? = null,
    val cardId: String? = null
): RemoteModel {
    fun toModel(): Work?{
        return convertTo {
            Work(workId!!, name!!, cardId!!)
        }
    }
}