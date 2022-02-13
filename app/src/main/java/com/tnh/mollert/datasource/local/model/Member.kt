package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Member(
    @PrimaryKey
    val email: String,
    var name: String,
    var avatar: String? = null,
    var biology: String? = null
) {

}