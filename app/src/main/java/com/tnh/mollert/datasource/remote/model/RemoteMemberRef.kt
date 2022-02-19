package com.tnh.mollert.datasource.remote.model

data class RemoteMemberRef(
    val email: String? = null,
    val ref: String? = null,
    val role: String = ROLE_OWNER
): RemoteModel {
    companion object{
        const val ROLE_OWNER = "owner"
        const val ROLE_MEMBER = "member"
        const val ROLE_LEADER = "leader"
    }
}