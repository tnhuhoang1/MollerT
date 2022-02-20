package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.List

data class RemoteList(
    val listId: String? = null,
    val name: String? = null,
    val ref: String? = null,
    val boardId: String? = null,
    val position: Int? = null,
    val status: String = List.STATUS_ACTIVE
): RemoteModel {
    fun toModel(): List?{
        return convertTo {
            List(
                listId!!,
                name!!,
                boardId!!,
                status,
                position!!
            )
        }
    }
}