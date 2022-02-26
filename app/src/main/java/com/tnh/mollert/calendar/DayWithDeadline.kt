package com.tnh.mollert.calendar

import com.tnh.mollert.datasource.local.model.Card

data class DayWithDeadline(
    var day: String,
    var dayNumber: String,
    var listCard: List<Card> = listOf()
) {
}