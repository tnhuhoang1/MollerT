package com.tnh.mollert.boardDetail.dashboard

data class Dashboard(
    var title: String = "",
    var max: Int = 1,
    var min: Int = 0,
    var listItem: List<DashboardItem> = listOf()
) {
}