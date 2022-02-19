package com.tnh.mollert.datasource.remote.model


class RemoteWorkspaceRef(
    val workspaceId: String? = null,
    val ref: String? = null,
    val role: String = ROLE_LEADER
): RemoteModel {
    companion object{
        const val ROLE_LEADER = "leader"
        const val ROLE_MEMBER = "member"
    }
}