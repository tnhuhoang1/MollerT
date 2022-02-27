package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Member

data class MemberAndBoard(
    @Embedded
    val member: Member,
    @Embedded
    val board: Board
) {

}