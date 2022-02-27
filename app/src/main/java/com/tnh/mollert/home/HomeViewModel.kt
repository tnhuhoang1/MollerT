package com.tnh.mollert.home

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.MemberWithWorkspaces
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.MessageMaker
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.datasource.remote.model.*
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.LabelPreset
import com.tnh.mollert.utils.UserWrapper
import com.tnh.mollert.utils.notifyBoardMember
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.log
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper
): BaseViewModel() {

    var memberWithWorkspaces = MutableLiveData<MemberWithWorkspaces>(null).toLiveData()
    val boards = repository.boardDao.countOneFlow().asLiveData()
    private var _loading = MutableLiveData(false)
    val loading = _loading.toLiveData()

    fun showProgress(){
        _loading.postValue(true)
    }

    fun hideProgress(){
        _loading.postValue(false)
    }

    suspend fun searchBoard(text: String): List<Board>{
        return repository.boardDao.searchBoard("%$text%")
    }

    fun loadMemberWithWorkspaces(){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            memberWithWorkspaces = repository.appDao.getMemberWithWorkspaces(email).asLiveData()
        }
    }

    fun syncWorkspacesAndBoardsDataFirstTime(
        pref: PrefManager
    ){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            if(pref.getString("$email+sync+all").isEmpty()){
                viewModelScope.launch {
                    // need strong internet connect to avoid error
                    repository.workspaceDao.countOne()?.let {
                        if(it == 0){
                            "Syncing all workspaces and boards data".logAny()
                            reloadWorkspaceFromRemote(email)
                            pref.putString("$email+sync+all", "synced")
                            "Synced".logAny()
                        }
                    }
                }
            }
        }
    }

    private suspend fun reloadWorkspaceFromRemote(email: String) {
        "Reloading all workspaces from remote".logAny()
        firestore.simpleGetDocumentModel<RemoteMember>(
            firestore.getMemberDoc(email)
        )?.let { rm->
            rm.workspaces?.forEach { ws->
                ws.ref?.let { ref->
                    saveWorkspaceFromRemote(email, ref)
                }
            }
        }
    }

    private suspend fun saveWorkspaceFromRemote(email: String, ref: String){
        firestore.simpleGetDocumentModel<RemoteWorkspace>(
            firestore.getDocRef(ref)
        )?.let {
            it.toModel()?.let { model->
                repository.workspaceDao.insertOne(model)
                saveMemberWorkspaceRelation(it.members, model.workspaceId)
                repository.memberWorkspaceDao.insertOne(MemberWorkspaceRel(email, model.workspaceId))
                saveAllBoardFromRemote(model.workspaceId)
            }
        }
    }

    private suspend fun saveMemberWorkspaceRelation(list: List<RemoteMemberRef>, workspaceId: String){
        list.forEach { rmr->
            rmr.email?.let { email->
                repository.memberWorkspaceDao.insertOne(MemberWorkspaceRel(email, workspaceId, rmr.role))
            }
        }
    }

    private suspend fun saveAllBoardFromRemote(workspaceId: String){
        firestore.getCol(firestore.getBoardCol(workspaceId))?.documentChanges?.forEach { docChange->
            val rb = docChange.document.toObject(RemoteBoard::class.java)
            rb.toModel()?.let { board->
                repository.boardDao.insertOne(board)
                rb.members?.let { listMember->
                    listMember.forEach { rmr->
                        rmr.ref?.let {
                            saveMemberAndRelationFromRemote(rmr.ref, board.boardId, rmr.role)
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveMemberAndRelationFromRemote(ref: String, boardId: String, role: String){
        firestore.simpleGetDocumentModel<RemoteMember>(
            firestore.getDocRef(ref)
        )?.let { rm->
            rm.toMember()?.let {
                repository.memberDao.insertOne(it)
                repository.memberBoardDao.insertOne(MemberBoardRel(it.email, boardId, role))
            }
        }
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
                    remoteActivity.activityType = Activity.TYPE_INVITATION
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

    private var lock = 0
    private var listBoard: List<List<Board>> = listOf()
    suspend fun getAllBoardOfUser(list: List<Workspace>): List<List<Board>>{
        if(lock <= 0){
            lock++
            listBoard = list.map {
                repository.appDao.getWorkspaceWithBoardsNoFlow(it.workspaceId).boards
            }
        }
        lock = 0
        return listBoard
    }

    fun createBoard(
        workspace: Workspace,
        boardName: String,
        visibility: String,
        background: String,
        onSuccess: ()-> Unit
    ){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            showProgress()
            val boardId = "${boardName}_${System.currentTimeMillis()}"
            val remoteBoard = RemoteBoard(
                boardId,
                workspace.workspaceId,
                boardName,
                "",
                background,
                Board.STATUS_OPEN,
                visibility,
                listOf(RemoteMemberRef(email, firestore.getMemberDoc(email).path)),
                listOf()
            )
            val boardDoc = firestore.getBoardDoc(workspace.workspaceId, boardId)
            viewModelScope.launch {
                if(firestore.addDocument(boardDoc, remoteBoard)){
                    LabelPreset.getPresetLabelList(boardId).forEach { remoteLabel->
                        val labelDoc = firestore.getLabelDoc(workspace.workspaceId, boardId, remoteLabel.labelId)
                        if(firestore.addDocument(
                            labelDoc,
                            remoteLabel
                        )){
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(email),
                                "labels",
                                labelDoc.path
                            )
                        }
                    }
                    UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                        val message = MessageMaker.getCreateBoardMessage(member.email, member.name, boardId, boardName)
                        val activityId = "created_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(workspace.workspaceId, boardId, activityId)
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            null,
                            message,
                            false,
                            Activity.TYPE_INFO,
                            System.currentTimeMillis()
                        )
                        if(firestore.addDocument(
                            activityDoc,
                            remoteActivity
                        )){
                            notifyBoardMember(repository, firestore, boardId, "activities", activityDoc.path)
                        }
                    }
                    if(visibility != Board.VISIBILITY_PRIVATE){
                        repository.appDao.getWorkspaceWithMembersNoFlow(workspace.workspaceId)?.members?.let { members->
                            "Notify to other members (${members.size}) about new board inserted".logAny()
                            members.forEach {
                                val trackingLoc = firestore.getTrackingDoc(it.email)
                                firestore.insertToArrayField(trackingLoc, "boards", boardDoc.path)
                            }
                            postMessage("Board added")
                            onSuccess()
                        }
                    }else{
                        val trackingLoc = firestore.getTrackingDoc(email)
                        firestore.insertToArrayField(trackingLoc, "boards", boardDoc.path)
                        postMessage("Board added")
                        onSuccess()
                    }
                    hideProgress()
                }else{
                    postMessage("ERROR")
                    hideProgress()
                }
            }
        }
    }

    suspend fun isJoinedThisBoard(boardId: String): Boolean{
        repository.appDao.getBoardWithMembers(boardId)?.let { boardWithMembers->
            UserWrapper.getInstance()?.currentUserEmail?.let { email->
                boardWithMembers.members.forEach { member->
                    if(member.email == email){
                        return true
                    }
                }
            }
        }
        return false
    }

    fun joinBoard(workspaceId: String, boardId: String){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            val boardRef = firestore.getBoardDoc(workspaceId, boardId)
            viewModelScope.launch {
                if(firestore.insertToArrayField(
                    boardRef,
                    "members",
                    RemoteMemberRef(email, firestore.getMemberDoc(email).path, RemoteMemberRef.ROLE_MEMBER)
                )){
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            val tracking = firestore.getTrackingDoc(mem.email)
                            firestore.insertToArrayField(
                                tracking,
                                "boards",
                                boardRef.path
                            )
                        }
                    }
                    firestore.insertToArrayField(
                        firestore.getTrackingDoc(email),
                        "boards",
                        boardRef.path
                    )
                    postMessage("Join board successfully")
                }
            }
        }
    }

}