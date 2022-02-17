package com.tnh.mollert.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.MemberWithWorkspaces
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.datasource.remote.model.RemoteBoard
import com.tnh.mollert.datasource.remote.model.RemoteMemberRef
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper
): BaseViewModel() {

    var memberWithWorkspaces = MutableLiveData<MemberWithWorkspaces>(null).toLiveData()
    private var _loading = MutableLiveData(false)
    val loading = _loading.toLiveData()

    fun showProgress(){
        _loading.postValue(true)
    }

    fun hideProgress(){
        _loading.postValue(false)
    }


    fun loadMemberWithWorkspaces(){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            memberWithWorkspaces = repository.appDao.getMemberWithWorkspaces(email).asLiveData()
        }
    }



    suspend fun getAllBoardOfUser(list: List<Workspace>): List<List<Board>>{
        return list.map {
            repository.appDao.getWorkspaceWithBoardsNoFlow(it.workspaceId).boards
        }
    }

    fun createBoard(workspace: Workspace, boardName: String){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            showProgress()
            val boardId = "${boardName}_${System.currentTimeMillis()}"
            val remoteBoard = RemoteBoard(
                boardId,
                boardName,
                "",
                "",
                Board.STATUS_OPEN,
                listOf(RemoteMemberRef(email, firestore.getMemberDoc(email).path)),
                listOf()
            )
            val boardDoc = firestore.getBoardDoc(workspace.workspaceId, boardId)
            viewModelScope.launch {
                if(firestore.addDocument(boardDoc, remoteBoard)){
                    repository.appDao.getWorkspaceWithMembersNoFlow(workspace.workspaceId)?.members?.let { members->
                        "Notify to other members about new board inserted".logAny()
                        members.forEach {
                            val trackingLoc = firestore.getTrackingDoc(it.email)
                            firestore.insertToArrayField(trackingLoc, "boards", boardDoc.path)
                            postMessage("Board added")
                        }
                    }
                    hideProgress()
                }else{
                    postMessage("ERROR")
                    hideProgress()
                }
            }
        }

    }


    override fun onCleared() {
        super.onCleared()
    }
}