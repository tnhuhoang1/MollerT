package com.tnh.mollert.notification

import androidx.lifecycle.viewModelScope
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.BoardAndCard
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.remote.model.*
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val firestore: FirestoreHelper,
    private val repository: AppRepository
): BaseViewModel() {

    var memberAndActivity = repository.appDao.getActivityAssocWithEmail(UserWrapper.getInstance()?.currentUserEmail ?: "")
    var notificationType: String = "single"
    suspend fun getCardById(cardId: String): BoardAndCard?{
        return repository.cardDao.getBoardAndCardByCardId(cardId)
    }

    suspend fun getMemberBoardRelByEmail(): List<MemberBoardRel>{
        return repository.memberBoardDao.getRelsByEmailId(UserWrapper.getInstance()?.currentUserEmail ?: "")
    }

    fun changeToYourNotification(){
        notificationType = "single"
        memberAndActivity = repository.appDao.getActivityAssocWithEmail(UserWrapper.getInstance()?.currentUserEmail ?: "")
    }

    fun changeToAllNotification(){
        notificationType = "all"
        memberAndActivity = repository.appDao.getAllMemberAndActivityByEmail()
    }

    fun joinWorkspace(
        ownerWorkspace: String,
        workspaceId: String,
        currentEmail: String,
    ){
        val currentUserDoc = firestore.getMemberDoc(currentEmail)
        val workspaceDoc = firestore.getWorkspaceDoc(workspaceId)
        viewModelScope.launch {
            firestore.simpleGetDocumentModel<RemoteWorkspace>(workspaceDoc)?.let { remoteWorkspace ->
                var isExisted = false
                remoteWorkspace.members.forEach { remoteMemberRef->
                    if(currentEmail == remoteMemberRef.email){
                        isExisted = true
                        postMessage("You already joined this workspace")
                        return@let
                    }
                }
                if(isExisted.not()){
                    val remoteWorkspaceRef = RemoteWorkspaceRef(workspaceId, workspaceDoc.path, RemoteWorkspaceRef.ROLE_MEMBER)
                    val remoteMemberRef = RemoteMemberRef(currentEmail, currentUserDoc.path, RemoteMemberRef.ROLE_MEMBER)
                    if(firestore.insertToArrayField(currentUserDoc, "workspaces", remoteWorkspaceRef)){
                        if(firestore.insertToArrayField(workspaceDoc, "members", remoteMemberRef)){
                            "Notify to fetch new data from remote".logAny()
                            remoteWorkspace.members.forEach { member->
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(member.email ?: currentEmail),
                                    "workspaces",
                                    workspaceDoc.path
                                )
                            }
                            if(firestore.insertToArrayField(
                                firestore.getTrackingDoc(currentEmail),
                                "workspaces",
                                workspaceDoc.path
                            )){
                                postMessage("Join workspace successfully")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * join board will join workspace automatically
     */
    fun joinBoard(
        workspaceId: String,
        boardId: String,
        currentEmail: String,
    ){
        val currentUserDoc = firestore.getMemberDoc(currentEmail)
        val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
        viewModelScope.launch {
            val workspaceDoc = firestore.getWorkspaceDoc(workspaceId)
            firestore.simpleGetDocumentModel<RemoteWorkspace>(workspaceDoc)?.let { remoteWorkspace ->
                var isExisted = false
                remoteWorkspace.members.forEach { remoteMemberRef->
                    if(currentEmail == remoteMemberRef.email){
                        isExisted = true
                    }
                }
                if(isExisted.not()){
                    val remoteWorkspaceRef = RemoteWorkspaceRef(workspaceId, workspaceDoc.path, RemoteWorkspaceRef.ROLE_MEMBER)
                    val remoteMemberRef = RemoteMemberRef(currentEmail, currentUserDoc.path, RemoteMemberRef.ROLE_MEMBER)
                    if(firestore.insertToArrayField(currentUserDoc, "workspaces", remoteWorkspaceRef)){
                        if(firestore.insertToArrayField(workspaceDoc, "members", remoteMemberRef)){
                            "Notify to fetch new data from remote".logAny()
                            remoteWorkspace.members.forEach { member->
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(member.email ?: currentEmail),
                                    "workspaces",
                                    workspaceDoc.path
                                )
                            }
                            if(firestore.insertToArrayField(
                                    firestore.getTrackingDoc(currentEmail),
                                    "workspaces",
                                    workspaceDoc.path
                                )){
                            }
                        }
                    }
                }
            }

            firestore.simpleGetDocumentModel<RemoteBoard>(boardDoc)?.let { remoteBoard ->
                remoteBoard.logAny()
                var isExisted = false
                remoteBoard.members?.forEach { remoteMemberRef->
                    if(remoteMemberRef.email == currentEmail){
                        isExisted = true
                        postMessage("You already joined this board")
                        return@let
                    }
                }
                if(remoteBoard.boardStatus == Board.STATUS_CLOSED){
                    postMessage("The board is closed")
                    cancel()
                }
                if(isExisted.not()){
                    val remoteMemberRef = RemoteMemberRef(currentEmail, currentUserDoc.path, RemoteMemberRef.ROLE_MEMBER)
                    if(firestore.insertToArrayField(boardDoc, "members", remoteMemberRef)){
                        "Notify to fetch new data from remote".logAny()
                        remoteBoard.members?.forEach { members->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(members.email ?: ""),
                                "boards",
                                boardDoc.path
                            )

                        }
                        firestore.insertToArrayField(
                            firestore.getTrackingDoc(currentEmail),
                            "boards",
                            boardDoc.path
                        )
                        postMessage("Join board successfully")
                    }
                }
            }
        }
    }
}