package com.tnh.mollert.datasource.local.relation

import androidx.room.Entity

@Entity(primaryKeys = ["email", "cardId"])
data class MemberCardRel(
    val email: String,
    val cardId: String
) {
}