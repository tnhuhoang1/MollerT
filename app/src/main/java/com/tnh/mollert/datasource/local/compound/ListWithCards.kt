package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import androidx.room.Relation
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.List

class ListWithCards(
    @Embedded
    val list: List,
    @Relation(
        parentColumn = "listId",
        entityColumn = "listId"
    )
    val cards: kotlin.collections.List<Card>
) {
}