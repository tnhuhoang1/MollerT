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

    val colorDataSet = listOf<ColorPreset>(
        ColorPreset("Green","#18BA80"),
        ColorPreset("Sky","#3EB8ED"),
        ColorPreset("Yellow","#FCD660"),
        ColorPreset("Orange","#EB8942"),
        ColorPreset("Red","#DF4146"),
        ColorPreset("Blue","#4957F2"),
        ColorPreset("Black","#000000"),
        ColorPreset("Purple","#7457DE"),
        ColorPreset("Pink","#F25F92"),
        ColorPreset("Pink","#2DCCC7"),
        ColorPreset("Tea","#B4C1CB"),
    )

    data class ColorPreset(
        val name: String,
        val color: String
    )
}