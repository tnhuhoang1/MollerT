package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Card(
    @PrimaryKey
    val cardId: Int,
    var cardName: String,
    var createdAt: Long = System.currentTimeMillis(),
    var status: String = STATUS_ACTIVE,
    var cardDesc: String? = null,
    var startDate: String? = null,
    var dueDate: String? = null,
    var checked: Boolean = false
) {
    companion object{
        const val STATUS_ACTIVE = "active"
        const val STATUS_ACHIEVED = "achieved"
    }
}