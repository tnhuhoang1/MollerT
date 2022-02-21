package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Workspace

data class RemoteWorkspace(
    val workspaceId: String? = null,
    val name: String? = null,
    val type: String? = null,
    val desc: String? = null,
    val members: List<RemoteMemberRef> = listOf()
): RemoteModel {
    fun toModel(): Workspace?{
        return convertTo {
            Workspace(
                workspaceId!!,
                name!!,
                desc,
                type!!
            )
        }
    }
}