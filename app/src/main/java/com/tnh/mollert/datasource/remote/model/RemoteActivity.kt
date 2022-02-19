package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Activity

data class RemoteActivity(
    var activityId: String? = null,
    var actor: String? = null,
    val boardId: String? = null,
    val cardId: String? = null,
    var message: String? = "",
    var seen: Boolean? = false,
    var activityType: String = "",
    val timestamp: Long? = null
): RemoteModel {

    fun toModel(): Activity?{
        return convertTo {
            Activity(
                activityId!!,
                actor!!,
                boardId,
                cardId,
                message!!,
                seen,
                activityType
            )
        }
    }
}