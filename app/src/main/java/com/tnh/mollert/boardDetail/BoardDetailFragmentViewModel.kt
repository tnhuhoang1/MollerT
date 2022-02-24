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
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.mollert.datasource.local.relation.MemberCardRel
import com.tnh.mollert.datasource.remote.model.*
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
                            firestore.insertToArrayField(
                                tracking,
                                "cards",
                                mapOf(
                                    "what" to "info",
                                    "ref" to cardLoc.path
                                )
                            )
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
            if(prefManager.getString("$email+$workspaceId+$boardId").isEmpty()){
                "Fetching all board content".logAny()
                viewModelScope.launch {
                    fetchAllLabels(workspaceId, boardId)
                    fetchAllList(workspaceId, boardId)
                    fetchAllActivity(workspaceId, boardId)
                    prefManager.putString("$email+$workspaceId+$boardId", "synced")
                }
            }
        }
    }

    private suspend fun fetchAllActivity(workspaceId: String, boardId: String) {
        val col = firestore.getActivityCol(workspaceId, boardId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteActivity::class.java)?.let { remoteActivity ->
                remoteActivity.toModel()?.let {
                    repository.activityDao.insertOne(it)
                }
            }
        }
    }

    private suspend fun fetchAllLabels(workspaceId: String, boardId: String): Boolean{
        val col = firestore.getLabelCol(workspaceId, boardId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteLabel::class.java)?.let { remoteLabel ->
                remoteLabel.toLabel()?.let {
                    repository.labelDao.insertOne(it)
                }
            }
        } ?: return false
        return true
    }

    private suspend fun fetchAllList(workspaceId: String, boardId: String){
        val col = firestore.getListCol(workspaceId, boardId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteList::class.java)?.let { remoteList ->
                remoteList.toModel()?.let {
                    repository.listDao.insertOne(it)
                }
            }
            fetchAllCardInList(workspaceId, boardId, document.id)
        }
    }

    private suspend fun fetchAllCardInList(workspaceId: String, boardId: String, listId: String){
        val col = firestore.getCardCol(workspaceId, boardId, listId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteCard::class.java)?.let { remoteCard ->
                remoteCard.toModel()?.let {
                    repository.cardDao.insertOne(it)
                    addCardLabelRel(it.cardId, remoteCard.labels)
                    addCardMemberRel(it.cardId, remoteCard.members)
                    fetchAttachmentsInCard(workspaceId, boardId, listId, it.cardId)
                }
            }
        }
    }

    private suspend fun fetchAttachmentsInCard(workspaceId: String, boardId: String, listId: String, cardId: String){
        val col = firestore.getAttachmentCol(workspaceId, boardId, listId, cardId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteAttachment::class.java)?.let { remoteAttachment ->
                remoteAttachment.toModel().let {
                    repository.attachmentDao.insertOne(it)
                }
            }
        }
    }

    private suspend fun addCardLabelRel(cardId: String, remoteCard: kotlin.collections.List<RemoteLabelRef>){
        repository.cardLabelDao.getRelByCardId(cardId).forEach {
            repository.cardLabelDao.deleteOne(it)
        }
        remoteCard.forEach { remoteLabelRef ->
            remoteLabelRef.labelId?.let {
                repository.cardLabelDao.insertOne(CardLabelRel(cardId, it))
            }
        }
    }

    private suspend fun addCardMemberRel(cardId: String, remoteCard: kotlin.collections.List<RemoteMemberRef>){
        repository.memberCarDao.getRelByCardId(cardId).forEach {
            repository.memberCarDao.deleteOne(it)
        }
        remoteCard.forEach { remoteMemberRef ->
            remoteMemberRef.email?.let { e->
                repository.memberCarDao.insertOne(
                    MemberCardRel(
                    e,
                    cardId,
                    remoteMemberRef.role
                )
                )
            }
        }
    }
}
