package com.tnh.mollert.datasource.remote.model

data class RemoteWorkspace(
    val workspaceId: String? = null,
    val name: String? = null,
    val members: List<RemoteMemberRef>? = null
): RemoteModel {

}