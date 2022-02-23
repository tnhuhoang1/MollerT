package com.tnh.mollert.cardDetail

import android.content.ContentResolver
import android.content.SyncStats
import android.net.Uri
import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentReference
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.CardWithLabels
import com.tnh.mollert.datasource.local.compound.MemberAndActivity
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.remote.model.RemoteActivity
import com.tnh.mollert.datasource.remote.model.RemoteAttachment
import com.tnh.mollert.datasource.remote.model.RemoteLabelRef
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardDetailFragmentViewModel @Inject constructor(
    private val firestore: FirestoreHelper,
    private val repository: AppRepository,
    private val storage: StorageHelper
): BaseViewModel() {

    private var email = UserWrapper.getInstance()?.currentUserEmail ?: ""

    var card: LiveData<Card> = MutableLiveData(null)
    private set

    var labels: LiveData<List<Label>> = MutableLiveData(null)
        private set

    var memberAndActivity: LiveData<List<MemberAndActivity>> = MutableLiveData(null)
    private set

    var cardWithLabels: LiveData<CardWithLabels> = MutableLiveData(null)

    var attachments: LiveData<List<Attachment>> = MutableLiveData(null)
    private set

    suspend fun getCardWithLabels(cardId: String): CardWithLabels{
        return repository.appDao.getCardWithLabels(cardId)
    }

    private var cardDoc: DocumentReference? = null

    fun setCardDoc(workspaceId: String, boardId: String, listId: String, cardId: String){
        cardDoc = firestore.getCardDoc(workspaceId, boardId, listId, cardId)
    }

    fun getCardById(cardId: String){
        card = repository.cardDao.getCardById(cardId).asLiveData()
        cardWithLabels = repository.appDao.getCardWithLabelsFlow(cardId).asLiveData()
        attachments = repository.attachmentDao.getAllByCardId(cardId).asLiveData()
        memberAndActivity = repository.appDao.getMemberAndActivityByCardIdFlow(cardId).asLiveData()
    }

    fun getLabelById(boardId: String){
        labels = repository.labelDao.getLabelsWithBoardId(boardId).asLiveData()
    }


    fun applyLabelsToCard(workspaceId: String, boardId: String, newList: List<Label>){
        val list = newList.map { label->
            val labelDoc = firestore.getLabelDoc(workspaceId, boardId, label.labelId)
            RemoteLabelRef(
                labelDoc.path,
                label.labelId
            )
        }
        viewModelScope.launch {
            cardDoc?.let { doc->
                if(firestore.mergeDocument(doc, mapOf("labels" to list))){
                    if(firestore.insertToArrayField(
                        firestore.getTrackingDoc(email),
                        "cards",
                            mapOf(
                                "what" to "label",
                                "ref" to doc.path
                            )
                    )){
                        postMessage("Set labels successfully")
                    }
                }
            }
        }
    }


    fun saveCardDescription(desc: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                    doc,
                    mapOf("desc" to desc)
                )){
                    if(firestore.insertToArrayField(
                            firestore.getTrackingDoc(email),
                            "cards",
                            mapOf(
                                "what" to "info",
                                "ref" to doc.path
                            )
                    )){
                        postMessage("Change description successfully")
                    }
                }
            }
        }
    }

    fun changeCardName(name: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                        doc,
                        mapOf("name" to name)
                    )){
                    if(firestore.insertToArrayField(
                            firestore.getTrackingDoc(email),
                            "cards",
                            mapOf(
                                "what" to "info",
                                "ref" to doc.path
                            )
                        )){
                        postMessage("Change name successfully")
                    }
                }
            }
        }
    }

    fun changeCardCover(contentResolver: ContentResolver, uri: Uri, cardId: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                storage.uploadCardCover(contentResolver, uri, cardId)?.let { url->
                    if(firestore.mergeDocument(
                        doc,
                        mapOf("cover" to url.toString())
                    )){
                        if(firestore.insertToArrayField(
                                firestore.getTrackingDoc(email),
                                "cards",
                                mapOf(
                                    "what" to "info",
                                    "ref" to doc.path
                                )
                            )){
                            postMessage("Change cover successfully")
                        }
                    }
                }
            }
        }
    }

    fun addImageAttachment(contentResolver: ContentResolver, uri: Uri, boardId: String, cardId: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                val attachmentId = "attachment_${System.currentTimeMillis()}"
                storage.uploadImageAttachment(contentResolver, uri, boardId, cardId, attachmentId)?.let { url->
                    val attDoc = firestore.getAttachmentDoc(doc, attachmentId)
                    val remoteAttachment = RemoteAttachment(
                        attachmentId,
                        "Image attachment",
                        Attachment.TYPE_IMAGE,
                        url.toString(),
                        cardId
                    )
                    if(firestore.addDocument(attDoc, remoteAttachment)){
                        if(firestore.insertToArrayField(
                                firestore.getTrackingDoc(email),
                                "attachments",
                                attDoc.path
                            )){
                            postMessage("Add attachment successfully")
                        }
                    }

                }
            }
        }
    }

    fun addLinkAttachment(cardId: String, link: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                val attachmentId = "attachment_${System.currentTimeMillis()}"
                val attDoc = firestore.getAttachmentDoc(doc, attachmentId)
                val remoteAttachment = RemoteAttachment(
                    attachmentId,
                    "Link attachment",
                    Attachment.TYPE_LINK,
                    link,
                    cardId
                )
                if(firestore.addDocument(attDoc, remoteAttachment)){
                    if(firestore.insertToArrayField(
                            firestore.getTrackingDoc(email),
                            "attachments",
                            attDoc.path
                        )){
                        postMessage("Add attachment successfully")
                    }
                }
            }
        }
    }

    fun saveDate(startDate: Long, dueDate: Long){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                    doc,
                    mapOf(
                        "startDate" to startDate,
                        "dueDate" to dueDate
                    )
                )){
                    if(firestore.insertToArrayField(
                        firestore.getTrackingDoc(email),
                        "cards",
                        mapOf(
                            "what" to "info",
                            "ref" to doc.path
                        )
                    )){
                        postMessage("Set date successfully")
                    }
                }
            }
        }
    }

    fun saveDateChecked(isChecked: Boolean){
        card.value?.let { c->
            if(c.checked != isChecked){
                cardDoc?.let { doc->
                    viewModelScope.launch {
                        if(firestore.mergeDocument(
                                doc,
                                mapOf(
                                    "checked" to isChecked,
                                )
                            )){
                            if(firestore.insertToArrayField(
                                    firestore.getTrackingDoc(email),
                                    "cards",
                                    mapOf(
                                        "what" to "info",
                                        "ref" to doc.path
                                    )
                                )){
                                "change checked".logAny()
                            }
                        }
                    }
                }
            }
        }
    }

    fun addComment(workspaceId: String, boardId: String, cardId:String, comment: String){
        val time = System.currentTimeMillis()
        val activityId = "comment_$time"
        val activityDoc = firestore.getActivityDoc(workspaceId, boardId, activityId)

        viewModelScope.launch {
            UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                card.value?.cardName?.let { cardName->
                    val message = MessageMaker.getCommentMessage(member.email, member.name, cardId, cardName, comment)
                    val remoteActivity = RemoteActivity(
                        activityId,
                        email,
                        boardId,
                        cardId,
                        message,
                        false,
                        Activity.TYPE_COMMENT,
                        time
                    )
                    if(firestore.addDocument(
                        activityDoc,
                        remoteActivity
                    )){
                        if(firestore.insertToArrayField(
                            firestore.getTrackingDoc(member.email),
                            "activities",
                            activityDoc.path
                        )){
                            "Posted a comment".logAny()
                        }
                    }
                }
            }
        }
    }
}