package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Automation(
    @PrimaryKey
    val automationId: String,
    var command: String,
    val email: String,
    var type: String,
    var boardId: String? = null,
    var cardId: String? = null
) {
    companion object{
        const val TYPE_TRIGGER = "trigger"
        const val TYPE_BOARD_BUTTON = "board_button"
        const val TYPE_CARD_BUTTON = "card_button"
    }
}