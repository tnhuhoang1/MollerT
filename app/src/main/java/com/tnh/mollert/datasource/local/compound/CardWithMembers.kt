package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.local.relation.MemberCardRel

data class CardWithMembers(
    @Embedded
    val card: Card,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "email",
        associateBy = Junction(MemberCardRel::class)
    )
    val members: List<Member>
) {
}