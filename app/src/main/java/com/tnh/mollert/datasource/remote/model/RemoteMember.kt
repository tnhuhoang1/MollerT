package com.tnh.mollert.datasource.remote.model

data class RemoteMember(
    val email: String,
    val name: String,
    val avatar: String?,
    val workspaces: List<RemoteWorkspaceRef>
): RemoteModel {
}