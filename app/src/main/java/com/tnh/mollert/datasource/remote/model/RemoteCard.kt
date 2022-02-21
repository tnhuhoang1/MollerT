package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Card

data class RemoteCard(
    val cardId: String? = null,
    val listId: String? = null,
    val name: String? = null,
    val desc: String? = "",
    val cover: String? = "",
    val autoCover: Boolean = true,
    val startDate: Long = 0L,
    val dueDate: Long = 0L,
    var checked: Boolean = false,
    var position: Int = 0,
    var createAt: Long = System.currentTimeMillis(),
    val status: String = STATUS_ACTIVE,
    val members: List<RemoteMemberRef> = listOf(),
    val labels: List<RemoteLabelRef> = listOf(),
    val activities: List<RemoteActivityRef> = listOf()
): RemoteModel {

    fun toModel(): Card?{
        return convertTo {
            Card(
                cardId!!,
                name!!,
                position,
                listId!!,
                createAt,
                status,
                desc!!,
                startDate,
                dueDate,
                checked,
                cover!!,
                autoCover
            )
        }
    }

    companion object{
        const val STATUS_ACTIVE = Card.STATUS_ACTIVE
        const val STATUS_ACHIEVED = Card.STATUS_ACHIEVED
    }
}