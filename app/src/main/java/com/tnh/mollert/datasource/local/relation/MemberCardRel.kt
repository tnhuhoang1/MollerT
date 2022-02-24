package com.tnh.mollert.datasource.local.relation

import androidx.room.Entity
import com.tnh.mollert.datasource.remote.model.RemoteMemberRef

@Entity(primaryKeys = ["email", "cardId"])
data class MemberCardRel(
    val email: String,
    val cardId: String,
    val role: String
) {
    fun toRemote(emailRef: String): RemoteMemberRef{
        return RemoteMemberRef(
            email,
            emailRef,
            role
        )
    }
}