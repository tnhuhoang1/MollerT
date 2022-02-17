package com.tnh.mollert.datasource.local.relation

import androidx.room.Entity

@Entity(primaryKeys = ["cardId", "labelId"])
data class CardLabelRel(
    val cardId: String,
    val labelId: String
) {
}