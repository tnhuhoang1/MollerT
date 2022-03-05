package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.List

data class ListAndCard(
    @Embedded
    val list: List,
    @Embedded
    val card: Card
) {
}