package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Card

data class RemoteCard(
    val cardId: String? = null,
    val name: String? = null,
    val desc: String? = null,
    val cover: String? = null,
    val autoCover: Boolean = true,
    val startDate: Long? = null,
    val dueDate: Long? = null,
    val status: String? = null,
    val labels: List<RemoteLabelRef>? = null,
    val activities: List<RemoteActivityRef>? = null
): RemoteModel {
    companion object{
        const val STATUS_ACTIVE = Card.STATUS_ACTIVE
        const val STATUS_ACHIEVED = Card.STATUS_ACHIEVED
    }
}