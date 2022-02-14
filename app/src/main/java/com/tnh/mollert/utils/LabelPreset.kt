package com.tnh.mollert.utils

import android.graphics.Color
import com.tnh.mollert.datasource.remote.model.RemoteLabel

object LabelPreset {
    val remoteData = listOf<RemoteLabel>(
        RemoteLabel(
            "label_green",
            "",
            "#18BA80"
        ),
        RemoteLabel(
            "label_red",
            "",
            "#DF4146"
        ),
        RemoteLabel(
            "label_sky",
            "",
            "#3EB8ED"
        ),
        RemoteLabel(
            "label_yellow",
            "",
            "#FCD660"
        ),
        RemoteLabel(
            "label_blue",
            "",
            "#4957F2"
        )
    )

    val colorDataSet = listOf<String>(
        "#18BA80",
        "#3EB8ED",
        "#FCD660",
        "#EB8942",
        "#DF414",
        "#4957F2",
        "#000000",
        "#7457DE",
        "#F25F92",
        "#2DCCC7",
        "#B4C1CB",
    )
}