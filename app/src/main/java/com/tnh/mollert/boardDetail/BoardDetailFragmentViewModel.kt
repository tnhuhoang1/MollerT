package com.tnh.mollert.boardDetail

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.*
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.BoardWithLists
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteBoard
import com.tnh.mollert.datasource.remote.model.RemoteCard
import com.tnh.mollert.datasource.remote.model.RemoteList
import com.tnh.mollert.datasource.remote.model.RemoteMemberRef
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardDetailFragmentViewModel @Inject constructor(
    private val firestore: FirestoreHelper,
    private val repository: AppRepository,
    private val storage: StorageHelper
): BaseViewModel() {

    var boardWithLists: LiveData<BoardWithLists> = MutableLiveData(null)

    fun getAllList(boardId: String){
        boardWithLists = repository.appDao.getBoardWithLists(boardId).asLiveData()
    }

    fun getConcatList(list: kotlin.collections.List<List>): kotlin.collections.List<List>{
        return list + List("null",
            "",
            "",
            "",
            list.size)
    }

    val boardBackground = Transformations.map(boardWithLists){
        it?.board?.background ?: ""
    }

    fun changeBoardBackground(workspaceId: String, boardId: String, contentResolver: ContentResolver, uri: Uri){
        val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
        viewModelScope.launch {
            storage.uploadBackgroundImage(workspaceId, boardId, contentResolver, uri)?.let { url->
                if(firestore.mergeDocument(
                    boardDoc,
                    mapOf(
                        "boardBackground" to url.toString()
                    )
                )){
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            val tracking = firestore.getTrackingDoc(mem.email)
                            firestore.insertToArrayField(tracking, "boards", boardDoc.path)
                            postMessage("Change background successfully")
                        }
                    }
                }
            }
        }
    }

    fun changeDescription(workspaceId: String, boardId: String, content: String){
        val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
        viewModelScope.launch {
            if(firestore.mergeDocument(
                    boardDoc,
                    mapOf(
                        "boardDesc" to content
                    )
                )){
                repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                    listMember.forEach { mem->
                        val tracking = firestore.getTrackingDoc(mem.email)
                        firestore.insertToArrayField(tracking, "boards", boardDoc.path)
                        postMessage("Change description successfully")
                    }
                }
            }
        }
    }

    fun createNewList(workspaceId: String, boardId: String, listName: String){
        val listId = "${boardId}_${listName}_${System.currentTimeMillis()}"
        val listDoc = firestore.getListDoc(workspaceId, boardId, listId)
        boardWithLists.value?.lists?.size?.let { position->
            val remoteList = RemoteList(
                listId,
                listName,
                listDoc.path,
                boardId,
                position
            )
            viewModelScope.launch {
                if(firestore.mergeDocument(listDoc, remoteList)){
                    // notify other members
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            val tracking = firestore.getTrackingDoc(mem.email)
                            firestore.insertToArrayField(tracking, "lists", listDoc.path)
                        }
                    }
                }else{
                    //failed
                    postMessage("Failed")
                }
            }
        }
    }

    fun createNewCard(workspaceId: String, boardId: String, listId: String, cardName: String){
        val cardId = "${listId}_${cardName}_${System.currentTimeMillis()}"
        val cardLoc = firestore.getCardDoc(workspaceId, boardId, listId, cardId)
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            val remoteCard = RemoteCard(
                cardId,
                listId,
                cardName,
                "",
                "",
                members = listOf(RemoteMemberRef(email, firestore.getMemberDoc(email).path, RemoteMemberRef.ROLE_CARD_CREATOR))
            )
            viewModelScope.launch {
                if(firestore.mergeDocument(cardLoc, remoteCard)){
                    // notify other members
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            val tracking = firestore.getTrackingDoc(mem.email)
                            firestore.insertToArrayField(tracking, "cards", cardLoc.path)
                        }
                    }
                }else{
                    //failed
                    postMessage("Failed")
                }
            }
        }
    }

    fun checkAndFetchList(prefManager: PrefManager, workspaceId: String, boardId: String){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            if(prefManager.getString("$email+$workspaceId").isEmpty()){
                "Fetching all board content".logAny()
            }
        }

    }


    fun getCardTest() : ArrayList<Card> {
        val a = arrayListOf<Card>()
        for (i in 1..10) {
            a.add(
                Card(
                    "hoang",
                    "card name $i",
                    i,
                    "",
                    dueDate = 1645343440321L,
                    cover = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/800px-Image_created_with_a_mobile_phone.png"
                )
            )
        }
        return a
    }
}
