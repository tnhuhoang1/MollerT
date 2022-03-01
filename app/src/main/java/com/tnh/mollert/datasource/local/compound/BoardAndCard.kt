package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Card

data class BoardAndCard(
    @Embedded
    val board: Board,
    @Embedded
    val card: Card
) {
}