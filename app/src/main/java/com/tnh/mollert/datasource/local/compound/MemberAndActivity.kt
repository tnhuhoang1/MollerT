package com.tnh.mollert.datasource.local.compound

import androidx.room.Embedded
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.mollert.datasource.local.model.Member

data class MemberAndActivity(
    @Embedded
    val member: Member,
    @Embedded
    val activity: Activity
) {
}