package com.tnh.mollert.notification

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.datasource.remote.model.RemoteMemberRef
import com.tnh.mollert.datasource.remote.model.RemoteWorkspaceRef
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val firestore: FirestoreHelper,
    private val repository: AppRepository
): BaseViewModel() {

    val memberAndActivity = repository.appDao.getActivityAssocWithEmail(UserWrapper.getInstance()?.currentUserEmail ?: "").asLiveData()



    fun joinWorkspace(
        ownerWorkspace: String,
        workspaceId: String,
        currentEmail: String,
    ){
        val currentUserDoc = firestore.getMemberDoc(currentEmail)
        val workspaceDoc = firestore.getWorkspaceDoc(workspaceId)
        viewModelScope.launch {
            firestore.simpleGetDocumentModel<RemoteMember>(currentUserDoc)?.let { remoteMember ->
                var isExisted = false
                remoteMember.workspaces?.forEach {
                    if(workspaceDoc.path == it.ref){
                        isExisted = true
                        postMessage("You already joined this workspace")
                        return@let
                    }
                }
                if(isExisted.not()){
                    val remoteWorkspaceRef = RemoteWorkspaceRef(
                        workspaceId, workspaceDoc.path, RemoteWorkspaceRef.ROLE_MEMBER
                    )
                    val remoteMemberRef = RemoteMemberRef(currentEmail, currentUserDoc.path, RemoteMemberRef.ROLE_MEMBER)

                    if(firestore.insertToArrayField(currentUserDoc, "workspaces", remoteWorkspaceRef)){
                        if(firestore.insertToArrayField(workspaceDoc, "members", remoteMemberRef)){
                            "Notify to fetch new data from remote".logAny()
                            repository.appDao.getWorkspaceWithMembersNoFlow(workspaceId)?.members?.let { members->
                                members.forEach { members->
                                    firestore.insertToArrayField(
                                        firestore.getTrackingDoc(members.email),
                                        "workspaces",
                                        workspaceDoc.path
                                    )
                                }
                            }
                            if(firestore.insertToArrayField(
                                firestore.getTrackingDoc(currentEmail),
                                "workspaces",
                                workspaceDoc.path
                            )){
                                postMessage("Join workspace successfully")
                            }
                        }
                    }else{
                        // failed
                    }
                }
            }
        }
    }
}