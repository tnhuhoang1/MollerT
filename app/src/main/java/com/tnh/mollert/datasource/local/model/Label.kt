package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Label(
    @PrimaryKey
    val cardId: String,
    var cardName: String
) {
}