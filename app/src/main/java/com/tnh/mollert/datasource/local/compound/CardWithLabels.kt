package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.Label
import com.tnh.mollert.datasource.local.relation.CardLabelRel

data class CardWithLabels(
    @Embedded
    val card: Card,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "labelId",
        associateBy = Junction(CardLabelRel::class)
    )
    val labels: List<Label>
) {
}