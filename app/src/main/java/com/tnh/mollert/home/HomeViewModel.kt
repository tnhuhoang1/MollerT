package com.tnh.mollert.home

import android.content.ContentResolver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.MemberWithWorkspaces
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
): BaseViewModel() {

    var memberWithWorkspaces = MutableLiveData<MemberWithWorkspaces>(null).toLiveData()
    val boards = repository.local.boardDao.countOneFlow().asLiveData()

    private var _loading = MutableLiveData(false)
    val loading = _loading.toLiveData()

    private fun showProgress(){
        _loading.postValue(true)
    }

    private fun hideProgress(){
        _loading.postValue(false)
    }

    suspend fun getBoardById(boardId: String): Board?{
        return repository.getBoardById(boardId)
    }

    suspend fun searchBoard(text: String): List<Board>{
        return repository.searchBoard("%$text%")
    }

    fun loadMemberWithWorkspaces(){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            memberWithWorkspaces = repository.getMemberWithWorkspaces(email)
        }
    }

    fun getMemberWorkspaceDao() = repository.getMemberWorkspaceDao()

    fun syncWorkspacesAndBoardsDataFirstTime(
        pref: PrefManager
    ){
        viewModelScope.launch {
            repository.syncWorkspacesAndBoardsDataFirstTime(pref)
        }
    }

    fun inviteMember(workspace: Workspace, otherEmail: String){
        viewModelScope.launch {
            postMessage(repository.inviteMember(workspace, otherEmail))
        }
    }

    private var lock = 0
    private var listBoard: List<List<Board>> = listOf()
    suspend fun getAllBoardOfUser(list: List<Workspace>): List<List<Board>>{
        if(lock <= 0){
            lock++
            listBoard = list.map {
                val l = repository.getWorkspaceWithBoardsNoFlow(it.workspaceId).boards
                withContext(Dispatchers.Default){
                    l.sortedBy { board->
                        board.boardName
                    }
                }
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
        backgroundMode: String,
        contentResolver: ContentResolver,
        onSuccess: ()-> Unit
    ){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            viewModelScope.launch {
                showProgress()
                repository.createBoard(
                    email,
                    workspace,
                    boardName,
                    visibility,
                    background,
                    backgroundMode,
                    contentResolver,
                    {
                        hideProgress()
                        postMessage("Add board failed")
                    },
                    {
                        hideProgress()
                        postMessage("Board added")
                        onSuccess()
                    }
                )
            }
        }
    }

    suspend fun isJoinedThisBoard(boardId: String) = repository.isJoinedThisBoard(boardId)

    suspend fun isOwnerOfThisBoard(boardId: String) = repository.isOwnerOfThisBoard(boardId)

    fun joinBoard(workspaceId: String, boardId: String, onSuccess: () -> Unit){
        viewModelScope.launch {
            repository.joinBoard(workspaceId, boardId){
                postMessage("Join board successfully")
                onSuccess()
            }
        }
    }

    fun reopenBoard(workspaceId: String, boardId: String){
        viewModelScope.launch {
            repository.reopenBoard(workspaceId, boardId){
                postMessage("Reopen board successfully")
            }
        }
    }

}