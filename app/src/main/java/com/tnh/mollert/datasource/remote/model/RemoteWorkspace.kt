package com.tnh.mollert.datasource.remote.model

data class RemoteWorkspace(
    val workspaceId: String,
    val name: String,
    val members: List<RemoteMemberRef>
) {

}