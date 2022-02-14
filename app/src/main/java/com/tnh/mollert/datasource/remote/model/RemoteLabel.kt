package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Label

data class RemoteLabel(
    val labelId: String? = null,
    val labelName: String? = null,
    val labelColor: String? = null,
): RemoteModel {
    fun toLabel(): Label?{
        return convertTo {
            Label(
                labelId!!,
                labelColor!!,
                labelName!!
            )
        }
    }
}