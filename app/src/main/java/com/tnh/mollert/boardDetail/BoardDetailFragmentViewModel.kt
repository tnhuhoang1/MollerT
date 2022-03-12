package com.tnh.mollert.boardDetail

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.BoardWithLists
import com.tnh.mollert.datasource.local.compound.MemberAndActivity
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardDetailFragmentViewModel @Inject constructor(
    private val repository: AppRepository,
): BaseViewModel() {
    private val boardCardHelper = BoardCardHelper(repository.local, repository.firestore)
    var boardWithLists: LiveData<BoardWithLists> = MutableLiveData(null)
    private set

    private val _isShowProgress = MutableLiveData(false)
    val isShowProgress = _isShowProgress.toLiveData()

    val email = UserWrapper.getInstance()?.currentUserEmail ?: ""

    fun showProgress(){
        _isShowProgress.postValue(true)
    }

    fun hideProgress(){
        _isShowProgress.postValue(false)
    }

    var isOwner: Boolean = false

    var cardArchived: LiveData<kotlin.collections.List<Card>> = MutableLiveData(null)

    var memberAndActivity: LiveData<kotlin.collections.List<MemberAndActivity>> = MutableLiveData(null)
    var boardDoc: DocumentReference? = null
    private set

    fun setBoardDoc(workspaceId: String, boardId: String){
        boardDoc = repository.getBoardDoc(workspaceId, boardId)
    }

    fun getAllList(boardId: String){
        boardWithLists = repository.local.appDao.getBoardWithLists(boardId).asLiveData()
        memberAndActivity = repository.local.appDao.getMemberAndActivityByBoardIdFlow(boardId).asLiveData()
        cardArchived = repository.local.cardDao.getCardsWithBoardId(boardId, Card.STATUS_ARCHIVED).asLiveData()
        viewModelScope.launch {
            repository.getBoardOwner(boardId)?.let { member->
                UserWrapper.getInstance()?.currentUserEmail?.let { email->
                    if(member.email == email){
                        isOwner = true
                    }
                }
            }
        }
    }

    suspend fun searchCard(search: String, boardId: String): kotlin.collections.List<Card>{
        return repository.searchCard(search, boardId)
    }

    fun getConcatList(list: kotlin.collections.List<List>): kotlin.collections.List<List>{
        return list.sortedBy { it.listName } + List("null",
            "",
            "",
            "",
            list.size)
    }

    fun changeBoardBackground(
        workspaceId: String,
        boardId: String,
        contentResolver: ContentResolver,
        uri: String,
        backgroundMode: String,
        onSuccess: () -> Unit
    ){
        viewModelScope.launch {
            boardWithLists.value?.board?.let { board ->
                showProgress()
                repository.changeBoardBackground(
                    workspaceId,
                    boardId,
                    board.boardName,
                    contentResolver,
                    uri,
                    backgroundMode,
                    {
                        postMessage("Something went wrong")
                    }
                ){
                    postMessage("Change background successfully")
                    onSuccess()
                }
                hideProgress()
            }
        }
    }

    fun changeDescription(workspaceId: String, boardId: String, content: String){
        viewModelScope.launch {
            boardWithLists.value?.board?.let { board->
                repository.changeDescription(
                    workspaceId,
                    boardId,
                    board.boardName,
                    content
                ){
                    postMessage("Change description successfully")
                }
            }
        }
    }

    fun createNewList(workspaceId: String, boardId: String, listName: String){
        boardWithLists.value?.let { boardWithList->
            boardDoc?.let { boardDoc->
                viewModelScope.launch {
                    repository.createNewList(
                        boardDoc,
                        boardWithList,
                        workspaceId,
                        boardId,
                        listName,
                        {
                            postMessage("Failed")
                        },
                        {

                        }
                    )
                }
            }
        }
    }

    fun createNewCard(workspaceId: String, boardId: String, listId: String, cardName: String){
        boardWithLists.value?.let { boardWithLists ->
            boardDoc?.let { boardDoc->
                viewModelScope.launch {
                    repository.createNewCard(
                        boardWithLists,
                        boardDoc,
                        workspaceId,
                        boardId,
                        listId,
                        cardName
                    ) {
                        postMessage("Failed")
                    }
                }
            }
        }
    }

    fun checkAndFetchList(prefManager: PrefManager, workspaceId: String, boardId: String){
        viewModelScope.launch {
            repository.checkAndFetchList(prefManager, workspaceId, boardId)
        }
    }

    fun leaveBoard(workspaceId: String, boardId: String, prefManager: PrefManager, onSuccess: ()-> Unit){
        boardWithLists.value?.let { boardWithLists ->
            boardDoc?.let { boardDoc->
                viewModelScope.launch {
                    repository.leaveBoard(
                        boardWithLists,
                        boardDoc,
                        workspaceId,
                        boardId,
                        prefManager,
                        onSuccess
                    )
                }
            }
        }
    }

    fun closeBoard(workspaceId: String, boardId: String, prefManager: PrefManager){
        viewModelScope.launch {
            repository.closeBoard(
                workspaceId,
                boardId,
                prefManager
            )
        }
    }

    fun archiveList(listId: String){
        boardWithLists.value?.let { b->
            viewModelScope.launch {
                showProgress()
                repository.archiveList(
                    b,
                    listId,
                    email,
                    boardCardHelper,
                ) {
                    postMessage("Cards are archived")
                }
                hideProgress()
            }
        }
    }

    fun changeVisibility(boardId: String, newVisibility: String){
        boardDoc?.let { doc->
            boardWithLists.value?.let { boardWithLists ->
                viewModelScope.launch {
                    repository.changeVisibility(
                        boardWithLists,
                        doc,
                        boardId,
                        newVisibility,
                    ){
                        postMessage("Changed to $newVisibility")
                    }
                }
            }
        }
    }

    fun inviteMemberToBoard(otherEmail: String, workspaceId: String){
        boardWithLists.value?.let {
            viewModelScope.launch {
                repository.inviteMemberToBoard(
                    it,
                    otherEmail,
                    workspaceId
                ).also {
                    postMessage(it)
                }
            }
        }
    }

    fun deleteList(list: List){
        boardWithLists.value?.let { b->
            viewModelScope.launch {
                showProgress()
                repository.deleteList(
                    this@BoardDetailFragmentViewModel,
                    email,
                    b,
                    boardCardHelper,
                    list
                ){
                    postMessage("Delete successfully")
                }
                hideProgress()
            }
        }

    }

    fun changeBoardName(name: String, workspaceId: String, boardId: String) {
        viewModelScope.launch {
            repository.changeBoardName(
                name,
                workspaceId,
                boardId,
                email
            ){
                postMessage("Change board name successfully")
            }
        }
    }

    fun changeListName(
        name: String,
        workspaceId: String,
        boardId: String,
        list: List
    ) {
        viewModelScope.launch {
            repository.changeListName(
                name,
                workspaceId,
                boardId,
                list,
                email
            ){
                postMessage("Change list name successfully")
            }
        }
    }

}
