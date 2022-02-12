package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Member(
    @PrimaryKey
    val email: String,
    val name: String,
    val avatar: String? = null,
    val biology: String? = null
) {

}