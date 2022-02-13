package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Card

data class RemoteCard(
    val cardId: String,
    val name: String,
    val desc: String,
    val cover: String,
    val startDate: Long,
    val dueDate: Long,
    val status: String,
    val labels: List<RemoteLabel>,
    val activities: List<RemoteActivityRef>
) {
    companion object{
        const val STATUS_ACTIVE = Card.STATUS_ACTIVE
        const val STATUS_ACHIEVED = Card.STATUS_ACHIEVED
    }
}