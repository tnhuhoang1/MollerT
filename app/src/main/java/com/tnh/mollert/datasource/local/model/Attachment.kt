package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Attachment(
    @PrimaryKey
    val attachment: String,
    var name: String,
    var type: String,
    var linkRemote: String,
    val cardId: String
) {
}