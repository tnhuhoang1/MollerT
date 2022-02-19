package com.tnh.mollert.datasource.remote.model

import com.tnh.mollert.datasource.local.model.Member

data class RemoteMember(
    val email: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val biography: String? = null,
    val workspaces: List<RemoteWorkspaceRef>? = listOf()
): RemoteModel {

    fun info(): Map<String, String?>{
        return mapOf(
            "email" to email,
            "name" to name,
            "avatar" to avatar,
            "biography" to biography
        )
    }
}


fun RemoteMember.toMember(): Member?{
    return convertTo {
        Member(email!!, name!!, avatar, biography)
    }
}
