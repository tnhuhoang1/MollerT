package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Card

data class RemoteCard(
    val cardId: String? = null,
    val listId: String? = null,
    val name: String? = null,
    val desc: String? = null,
    val cover: String? = null,
    val autoCover: Boolean = true,
    val startDate: Long? = null,
    val dueDate: Long? = null,
    var checked: Boolean = false,
    val status: String = STATUS_ACTIVE,
    val members: List<RemoteMemberRef> = listOf(),
    val labels: List<RemoteLabelRef> = listOf(),
    val activities: List<RemoteActivityRef> = listOf()
): RemoteModel {
    companion object{
        const val STATUS_ACTIVE = Card.STATUS_ACTIVE
        const val STATUS_ACHIEVED = Card.STATUS_ACHIEVED
    }
}