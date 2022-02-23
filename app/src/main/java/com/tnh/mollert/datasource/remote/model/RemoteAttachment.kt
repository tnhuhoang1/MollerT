package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Attachment

data class RemoteAttachment(
    val attachmentId: String = "",
    val name: String = "",
    val type: String = Attachment.TYPE_LINK,
    val linkRemote: String = "",
    val cardId: String = ""
): RemoteModel {
    fun toModel(): Attachment{
        return Attachment(
            attachmentId, name, type, linkRemote, cardId
        )
    }
}