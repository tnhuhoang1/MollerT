package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.Label
import com.tnh.mollert.datasource.local.relation.CardLabelRel

data class LabelWithCards(
    @Embedded
    val label: Label,
    @Relation(
        parentColumn = "labelId",
        entityColumn = "cardId",
        associateBy = Junction(CardLabelRel::class)
    )
    val cards: List<Card>
) {
}