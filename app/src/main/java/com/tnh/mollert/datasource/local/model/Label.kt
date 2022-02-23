package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tnh.mollert.datasource.remote.model.RemoteLabelRef

@Entity
data class Label(
    @PrimaryKey
    val labelId: String,
    var labelColor: String,
    var labelName: String = "",
    var boardId: String
) {

}