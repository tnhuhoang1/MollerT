package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Card(
    @PrimaryKey
    val cardId: String,
    var cardName: String,
    var position: Int,
    val listIdPar: String,
    var createdAt: Long = System.currentTimeMillis(),
    var cardStatus: String = STATUS_ACTIVE,
    var cardDesc: String? = null,
    var startDate: Long = 0L,
    var dueDate: Long = 0L,
    var checked: Boolean = false,
    var cover: String = "",
    var autoCover: Boolean = true
) {
    companion object{
        const val STATUS_ACTIVE = "active"
        const val STATUS_ACHIEVED = "achieved"
    }
}