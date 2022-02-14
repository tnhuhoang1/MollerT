package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Member

data class RemoteMember(
    val email: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val biology: String? = null,
    val workspaces: List<RemoteWorkspaceRef>? = null
): RemoteModel {
}


fun RemoteMember.toMember(): Member?{
    return convertTo {
        Member(email!!, name!!, avatar, biology)
    }
}
