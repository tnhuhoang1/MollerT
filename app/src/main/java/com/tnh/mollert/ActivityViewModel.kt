package com.tnh.mollert

import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.datasource.remote.model.*
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper
): BaseViewModel() {
    private var workspaceListener: ListenerRegistration? = null
    private var boardListener: ListenerRegistration? = null

    fun registerRemoteEvent(){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            workspaceListener = firestore.listenDocument(
                firestore.getTrackingDoc(email),
                {
                    trace(it)
                }
            ){ snap->
                snap?.data?.let { map->
                    registerWorkspace(email, map)
                    registerBoard(email, map)
                    registerInvitation(email, map)
                }
            }
        }
    }

    private fun registerWorkspace(email: String, map: Map<String, Any>){
        (map["workspaces"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach {
                    viewModelScope.launch {
                        saveWorkspaceFromRemote(email, it)
                        firestore.removeFromArrayField(firestore.getTrackingDoc(email), "workspaces", it)
                    }
                }
            }else{
                viewModelScope.launch {
                    repository.workspaceDao.countOne()?.let {
                        if(it == 0){
                            reloadWorkspaceFromRemote(email)
                        }
                    }
                }
            }
        }
    }

    private fun registerBoard(email: String, map: Map<String, Any>){
        (map["boards"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach {
                    viewModelScope.launch {
                        firestore.getDocRef(it).parent.parent?.id?.let { wsId->
                            saveBoardFromRemote(it, wsId)
                            firestore.removeFromArrayField(firestore.getTrackingDoc(email), "boards", it)
                        }
                    }
                }
            }else{
                viewModelScope.launch {
                    repository.boardDao.countOne()?.let {
                        if(it == 0){
                            "Reloading all boards from remote".logAny()
                            reloadBoardFromRemote(email)
                        }
                    }
                }
            }
        }
    }

    private fun registerInvitation(email: String, map: Map<String, Any>){
        (map["invitations"] as List<Map<String, Any>>?)?.let { listActivity->
            if(listActivity.isNotEmpty()){
                listActivity.forEach { ra ->
                    try {
                        val remoteActivity = RemoteActivity(
                            ra["activityId"] as String,
                            ra["actor"] as String,
                            ra["boardId"] as String?,
                            ra["cardId"] as String?,
                            ra["message"] as String,
                            ra["seen"] as Boolean,
                            ra["activityType"] as String,
                            ra["timestamp"] as Long,

                        )
                        viewModelScope.launch {
                            remoteActivity.toModel()?.let {
                                repository.activityDao.insertOne(it)
                                firestore.removeFromArrayField(firestore.getTrackingDoc(email), "invitations", remoteActivity)
                            }
                        }
                        remoteActivity.logAny()
                    }catch (e: Exception){
                        trace(e)
                    }
                }
            }
        }
    }

    private suspend fun saveBoardFromRemote(ref: String, workspaceId: String){
        firestore.simpleGetDocumentModel<RemoteBoard>(
            firestore.getDocRef(ref)
        )?.let {
            it.toModel(workspaceId)?.let { model->
                repository.boardDao.insertOne(model)
                it.members?.let { listMember->
                    listMember.forEach { rmr->
                        rmr.ref?.let {
                            saveMemberAndRelationFromRemote(rmr.ref, model.boardId)
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveMemberAndRelationFromRemote(ref: String, boardId: String){
        firestore.simpleGetDocumentModel<RemoteMember>(
            firestore.getDocRef(ref)
        )?.let { rm->
            rm.toMember()?.let {
                repository.memberDao.insertOne(it)
                repository.memberBoardDao.insertOne(MemberBoardRel(it.email, boardId))
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

    private suspend fun reloadBoardFromRemote(email: String){
        repository.appDao.getMemberWithWorkspacesNoFlow(email)?.workspaces?.forEach {
            saveAllBoardFromRemote(it.workspaceId)
        }
    }

    private suspend fun saveAllBoardFromRemote(workspaceId: String){
        firestore.getCol(firestore.getBoardCol(workspaceId))?.documentChanges?.forEach { docChange->
            val rb = docChange.document.toObject(RemoteBoard::class.java)
            rb.toModel(workspaceId)?.let { board->
                repository.boardDao.insertOne(board)
                rb.members?.let { listMember->
                    listMember.forEach { rmr->
                        rmr.ref?.let {
                            saveMemberAndRelationFromRemote(rmr.ref, board.boardId)
                        }
                    }
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
                repository.memberWorkspaceDao.insertOne(MemberWorkspaceRel(email, model.workspaceId))
                saveAllBoardFromRemote(model.workspaceId)
            }
        }
    }

    private fun unregisterRemoteEvent(){
        workspaceListener?.remove()
        workspaceListener = null
    }

    override fun onCleared() {
        super.onCleared()
        unregisterRemoteEvent()
    }
}