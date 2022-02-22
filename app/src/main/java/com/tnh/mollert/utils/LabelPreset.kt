package com.tnh.mollert.utils

import com.tnh.mollert.datasource.remote.model.RemoteLabel

object LabelPreset {

    fun getPresetLabelList(boardId: String): List<RemoteLabel>{
        return listOf(
            RemoteLabel(
                "${boardId}_green",
                "Done",
                "#18BA80"
            ),
            RemoteLabel(
                "${boardId}_red",
                "Late",
                "#DF4146"
            ),
            RemoteLabel(
                "${boardId}_sky",
                "Working",
                "#3EB8ED"
            ),
            RemoteLabel(
                "${boardId}_yellow",
                "Invalid",
                "#FCD660"
            ),
            RemoteLabel(
                "${boardId}_blue",
                "Seen",
                "#4957F2"
            )
        )
    }

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