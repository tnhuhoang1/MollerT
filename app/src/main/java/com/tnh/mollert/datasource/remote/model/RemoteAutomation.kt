package com.tnh.mollert.datasource.remote.model

class RemoteAutomation(
    val automationId: String,
    val command: String,
    val type: String,
    val assocBoard: String,
    val assocCard: String
) {
}