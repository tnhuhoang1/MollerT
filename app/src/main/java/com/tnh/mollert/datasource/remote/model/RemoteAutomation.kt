package com.tnh.mollert.datasource.remote.model

class RemoteAutomation(
    val automationId: String? = null,
    val command: String? = null,
    val type: String? = null,
    val assocBoard: String? = null,
    val assocCard: String? = null
): RemoteModel {
}