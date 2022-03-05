package com.tnh.mollert.home.manage

import android.util.Patterns
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.WorkspaceWithMembers
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.MessageMaker
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.datasource.remote.model.*
import com.tnh.mollert.home.CreateBoardDialog
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.LabelPreset
import com.tnh.mollert.utils.UserWrapper
import com.tnh.mollert.utils.notifyBoardMember
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageWorkspaceViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper
): BaseViewModel() {
    var homeWorkspace: LiveData<WorkspaceWithMembers> = MutableLiveData(null)
    private set

    fun loadWorkspace(workspaceId: String){
        homeWorkspace = repository.appDao.getWorkspaceWithMembers(workspaceId).asLiveData()
    }

    suspend fun getWorkspaceOwner(workspaceId: String): MemberWorkspaceRel?{
        return repository.memberWorkspaceDao.getWorkspaceLeader(workspaceId)
    }

    private val _progress = MutableLiveData(false)
    val progress = _progress.toLiveData()

    fun showProgress(){
        _progress.postValue(true)
    }

    fun hideProgress(){
        _progress.postValue(false)
    }

    fun inviteMember(workspace: Workspace, otherEmail: String){
        viewModelScope.launch {
            UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                val email = member.email
                if (email == otherEmail) {
                    postMessage("You can not invite yourself")
                    cancel()
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(otherEmail).matches()) {
                    postMessage("Invalid email address")
                    cancel()
                }
                firestore.simpleGetDocumentModel<RemoteMember>(firestore.getMemberDoc(otherEmail))?.toMember()?.let { m ->
                    val id = "invitation_${workspace.workspaceId}_"
                    val remoteActivity = RemoteActivity(
                        id + System.currentTimeMillis(),
                        email,
                        null,
                        null,
                        "",
                        false,
                        Activity.TYPE_INFO,
                        System.currentTimeMillis()
                    )
                    remoteActivity.message = MessageMaker.getWorkspaceInvitationSenderMessage(
                        workspace.workspaceId,
                        workspace.workspaceName,
                        m.email,
                        m.name
                    )
                    sendNotification(email, remoteActivity)
                    remoteActivity.activityId = id + System.currentTimeMillis()
                    remoteActivity.actor = otherEmail
                    remoteActivity.activityType = Activity.TYPE_INVITATION_WORKSPACE
                    remoteActivity.message = MessageMaker.getWorkspaceInvitationReceiverMessage(
                        workspace.workspaceId,
                        workspace.workspaceName,
                        member.email,
                        member.name
                    )
                    sendNotification(otherEmail, remoteActivity)
                    postMessage("Sent invitation successfully")
                } ?: postMessage("No such member exist")
            }
        }
    }

    private suspend fun sendNotification(email: String, remoteActivity: RemoteActivity){
        firestore.insertToArrayField(
            firestore.getTrackingDoc(email),
            "invitations",
            remoteActivity
        )
    }

    fun changeName(name: String, workspaceId: String){
        showProgress()
        val workspaceDoc = firestore.getWorkspaceDoc(workspaceId)
        viewModelScope.launch {
            val data = mapOf<String, Any>(
                "name" to name
            )
            if(firestore.mergeDocument(workspaceDoc, data)){
                repository.appDao.getWorkspaceWithMembersNoFlow(workspaceId)?.members?.let { members->
                    "Notify to other members (${members.size}) about info changed".logAny()
                    members.forEach {
                        val trackingLoc = firestore.getTrackingDoc(it.email)
                        firestore.insertToArrayField(trackingLoc, "workspaces", mapOf(
                            "what" to "info",
                            "ref" to workspaceDoc.path
                        ))
                    }
                }
            }
            hideProgress()
        }
    }

    fun changeDesc(desc: String, workspaceId: String) {
        showProgress()
        val workspaceDoc = firestore.getWorkspaceDoc(workspaceId)
        viewModelScope.launch {
            val data = mapOf<String, Any>(
                "desc" to desc
            )
            if(firestore.mergeDocument(workspaceDoc, data)){
                repository.appDao.getWorkspaceWithMembersNoFlow(workspaceId)?.members?.let { members->
                    "Notify to other members (${members.size}) about info changed".logAny()
                    members.forEach {
                        val trackingLoc = firestore.getTrackingDoc(it.email)
                        firestore.insertToArrayField(trackingLoc, "workspaces", mapOf(
                            "what" to "info",
                            "ref" to workspaceDoc.path
                        ))
                    }
                }
            }
            hideProgress()
        }
    }
}