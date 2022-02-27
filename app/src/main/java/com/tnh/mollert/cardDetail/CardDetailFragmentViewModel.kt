package com.tnh.mollert.cardDetail

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.CardWithLabels
import com.tnh.mollert.datasource.local.compound.CardWithMembers
import com.tnh.mollert.datasource.local.compound.MemberAndActivity
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.remote.model.*
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

    var email = UserWrapper.getInstance()?.currentUserEmail ?: ""
    private set

    var user: Member? = null
    private set

    var card: LiveData<Card> = MutableLiveData(null)
    private set

    var labels: LiveData<List<Label>> = MutableLiveData(null)
        private set

    var memberAndActivity: LiveData<List<MemberAndActivity>> = MutableLiveData(null)
    private set

    var cardWithMembers: LiveData<CardWithMembers> = MutableLiveData(null)
        private set

    var cardWithLabels: LiveData<CardWithLabels> = MutableLiveData(null)
    private set

    var works: LiveData<List<Work>> = MutableLiveData(null)
    private set

    var attachments: LiveData<List<Attachment>> = MutableLiveData(null)
    private set

    suspend fun getCardWithLabels(cardId: String): CardWithLabels{
        return repository.appDao.getCardWithLabels(cardId)
    }

    private var cardDoc: DocumentReference? = null
    private var boardDoc: DocumentReference = firestore.getBoardDoc("stub", "stub")
    private var board: Board? = null
    private var _cardId: String = ""
    fun setCardDoc(workspaceId: String, boardId: String, listId: String, cardId: String){
        _cardId = cardId
        cardDoc = firestore.getCardDoc(workspaceId, boardId, listId, cardId)
        boardDoc = firestore.getBoardDoc(workspaceId, boardId)
        viewModelScope.launch {
            board = repository.boardDao.getBoardByIdNoFlow(boardId)
            user = UserWrapper.getInstance()?.getCurrentUser()
        }
    }

    fun getCardById(cardId: String){
        card = repository.cardDao.getCardById(cardId).asLiveData()
        cardWithLabels = repository.appDao.getCardWithLabelsFlow(cardId).asLiveData()
        attachments = repository.attachmentDao.getAllByCardId(cardId).asLiveData()
        memberAndActivity = repository.appDao.getMemberAndActivityByCardIdFlow(cardId).asLiveData()
        cardWithMembers = repository.appDao.getCardWithMembersByCardIdFlow(cardId).asLiveData()
        works = repository.workDao.getWorksByCardIdFlow(cardId).asLiveData()
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
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                    val message = MessageMaker.getChangedLabelMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                    val remoteActivity = RemoteActivity(
                        activityId,
                        user?.email,
                        boardId,
                        _cardId,
                        message,
                        false,
                        Activity.TYPE_ACTION,
                        System.currentTimeMillis()
                    )
                    firestore.addDocument(activityDoc, remoteActivity)

                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "cards",
                                mapOf(
                                    "what" to "label",
                                    "ref" to doc.path
                                )
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Set labels successfully")
                    }
                }
            }
        }
    }


    fun saveCardDescription(boardId: String, desc: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                    doc,
                    mapOf("desc" to desc)
                )){
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc, activityId)

                    val message = MessageMaker.getChangedCardDescMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                    val remoteActivity = RemoteActivity(
                        activityId,
                        user?.email,
                        boardId,
                        _cardId,
                        message,
                        false,
                        Activity.TYPE_ACTION,
                        System.currentTimeMillis()
                    )
                    firestore.addDocument(activityDoc, remoteActivity)

                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->

                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "cards",
                                mapOf(
                                    "what" to "info",
                                    "ref" to doc.path
                                )
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Change description successfully")
                    }
                }
            }
        }
    }

    fun changeCardName(boardId: String, name: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                    doc,
                    mapOf("name" to name)
                )){
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                    val message = MessageMaker.getChangedCardNameMessage(_cardId, card.value?.cardName.toString(), name, boardId, board?.boardName.toString())
                    val remoteActivity = RemoteActivity(
                        activityId,
                        email,
                        boardId,
                        _cardId,
                        message,
                        false,
                        Activity.TYPE_ACTION,
                        System.currentTimeMillis()
                    )
                    firestore.addDocument(activityDoc, remoteActivity)

                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->


                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "cards",
                                mapOf(
                                    "what" to "info",
                                    "ref" to doc.path
                                )
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Change name successfully")
                    }
                }
            }
        }
    }

    fun changeCardCover(boardId: String, contentResolver: ContentResolver, uri: Uri, cardId: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                storage.uploadCardCover(contentResolver, uri, cardId)?.let { url->
                    if(firestore.mergeDocument(
                        doc,
                        mapOf("cover" to url.toString())
                    )){
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getChangedCardCoverMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            _cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)

                        repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                            listMember.forEach { mem->

                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "cards",
                                    mapOf(
                                        "what" to "info",
                                        "ref" to doc.path
                                    )
                                )
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "activities",
                                    activityDoc.path
                                )
                            }
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
                        repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                            val activityId = "activity_${System.currentTimeMillis()}"
                            val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                            val message = MessageMaker.getAttachedImageMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                            val remoteActivity = RemoteActivity(
                                activityId,
                                email,
                                boardId,
                                _cardId,
                                message,
                                false,
                                Activity.TYPE_ACTION,
                                System.currentTimeMillis()
                            )
                            firestore.addDocument(activityDoc, remoteActivity)

                            listMember.forEach { mem->


                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "attachments",
                                    attDoc.path
                                )
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "activities",
                                    activityDoc.path
                                )
                            }
                            postMessage("Add attachment successfully")
                        }
                    }
                }
            }
        }
    }

    fun addLinkAttachment(boardId: String, cardId: String, link: String){
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
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getAttachedLinkMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            _cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)

                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "attachments",
                                attDoc.path
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Add attachment successfully")
                    }
                }
            }
        }
    }

    fun saveDate(boardId: String, startDate: Long, dueDate: Long){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                    doc,
                    mapOf(
                        "startDate" to startDate,
                        "dueDate" to dueDate
                    )
                )){
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getCardAddDateMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            _cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)

                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "cards",
                                mapOf(
                                    "what" to "info",
                                    "ref" to doc.path
                                )
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Set date successfully")
                    }
                }
            }
        }
    }

    fun saveDateChecked(boardId: String, isChecked: Boolean){
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
                            repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                                val activityId = "activity_${System.currentTimeMillis()}"
                                val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                                val message =
                                    if (isChecked)
                                        MessageMaker.getMarkCheckedDateMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                                    else
                                        MessageMaker.getMarkUncheckedDateMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                                val remoteActivity = RemoteActivity(
                                    activityId,
                                    email,
                                    boardId,
                                    _cardId,
                                    message,
                                    false,
                                    Activity.TYPE_ACTION,
                                    System.currentTimeMillis()
                                )
                                firestore.addDocument(activityDoc, remoteActivity)

                                listMember.forEach { mem->
                                    firestore.insertToArrayField(
                                        firestore.getTrackingDoc(mem.email),
                                        "cards",
                                        mapOf(
                                            "what" to "info",
                                            "ref" to doc.path
                                        )
                                    )
                                    firestore.insertToArrayField(
                                        firestore.getTrackingDoc(mem.email),
                                        "activities",
                                        activityDoc.path
                                    )
                                }
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
            card.value?.cardName?.let { cardName->
                val message = MessageMaker.getCommentMessage(cardId, cardName, comment)
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
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        "Posted a comment".logAny()
                    }
                }
            }
        }
    }

    fun achieveCard(boardId: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                        doc,
                        mapOf(
                            "status" to Card.STATUS_ACHIEVED,
                        )
                    )){
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                    val message = MessageMaker.getCardAchievedMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                    val remoteActivity = RemoteActivity(
                        activityId,
                        email,
                        boardId,
                        _cardId,
                        message,
                        false,
                        Activity.TYPE_ACTION,
                        System.currentTimeMillis()
                    )
                    firestore.addDocument(activityDoc, remoteActivity)

                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "cards",
                                mapOf(
                                    "what" to "info",
                                    "ref" to doc.path
                                )
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Card achieved")
                    }
                }
            }
        }
    }

    fun activateCard(boardId: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                        doc,
                        mapOf(
                            "status" to Card.STATUS_ACTIVE,
                        )
                    )){

                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                    val message = MessageMaker.getActiveCardMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                    val remoteActivity = RemoteActivity(
                        activityId,
                        email,
                        boardId,
                        _cardId,
                        message,
                        false,
                        Activity.TYPE_ACTION,
                        System.currentTimeMillis()
                    )
                    firestore.addDocument(activityDoc, remoteActivity)

                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "cards",
                                mapOf(
                                    "what" to "info",
                                    "ref" to doc.path
                                )
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Card activated")
                    }
                }
            }
        }
    }

    fun joinCard(boardId: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.insertToArrayField(
                    doc,
                    "members",
                    RemoteMemberRef(
                        email,
                        firestore.getMemberDoc(email).path,
                        RemoteMemberRef.ROLE_MEMBER
                    )
                )){
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getJoinedCardMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            _cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)

                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "cards",
                                mapOf(
                                    "what" to "member",
                                    "ref" to doc.path
                                )
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Join successfully")
                    }
                }
            }
        }
    }

    fun leaveCard(boardId: String, cardId: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                repository.memberCarDao.getRelByEmailAndCardId(email, cardId)?.let { memberCardRel ->
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getLeftCardMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            _cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)

                        listMember.forEach { mem->
                            if(firestore.removeFromArrayField(
                                doc,
                                "members",
                                memberCardRel.toRemote(firestore.getMemberDoc(mem.email).path)
                            )){
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "cards",
                                    mapOf(
                                        "what" to "member",
                                        "ref" to doc.path
                                    )
                                )
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "activities",
                                    activityDoc.path
                                )
                            }
                        }
                        postMessage("Leave successfully")
                    }
                }
            }
        }
    }


    fun addWork(boardId: String, cardId: String, workName: String) {
        val workId = "${workName}_${System.currentTimeMillis()}"
        cardDoc?.let { doc->
            val workDoc = firestore.getWorkDoc(doc, workId)
            val remoteWork = RemoteWork(workId, workName, workDoc.path, cardId)
            viewModelScope.launch {
                if(firestore.addDocument(
                    workDoc,
                    remoteWork
                )){
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getAddWorkMessage(workName, _cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            _cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)

                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "works",
                                workDoc.path
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Add work successfully")
                    }
                }
            }
        }
    }

    fun addTask(boardId: String, workId: String, taskName: String){
        val taskId = "${taskName}_${System.currentTimeMillis()}"
        cardDoc?.let { doc->
            val taskDoc = firestore.getTaskDoc(doc, workId, taskId)
            val remoteTask = RemoteTask(taskId, taskName, workId)
            viewModelScope.launch {
                if(firestore.addDocument(
                    taskDoc,
                    remoteTask
                )){
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getAddTaskMessage(taskName, _cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            _cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)

                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "tasks",
                                taskDoc.path
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc.path
                            )
                        }
                        postMessage("Add task successfully")
                    }
                }
            }
        }
    }

    fun deleteTask(boardId: String, task: Task, isShow: Boolean = true){
        cardDoc?.let { doc->
            val taskDoc = firestore.getTaskDoc(doc, task.workId, task.taskId)
            viewModelScope.launch {
                if(firestore.deleteDocument(
                        taskDoc,
                )){
                    if(repository.taskDao.deleteOne(task) > 0){
                        repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                            val activityId = "activity_${System.currentTimeMillis()}"
                            val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                            val message = MessageMaker.getDelTaskMessage(task.taskName, _cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                            val remoteActivity = RemoteActivity(
                                activityId,
                                email,
                                boardId,
                                _cardId,
                                message,
                                false,
                                Activity.TYPE_ACTION,
                                System.currentTimeMillis()
                            )
                            firestore.addDocument(activityDoc, remoteActivity)

                            listMember.forEach { mem->
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "delTasks",
                                    taskDoc.path
                                )
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "activities",
                                    activityDoc.path
                                )
                            }
                            if(isShow){
                                postMessage("Delete task successfully")
                            }
                        }
                    }
                }
            }
        }
    }

    fun onTaskChecked(task: Task, newValue: Boolean){
        if(task.checked != newValue){
            cardDoc?.let { doc->
                val taskDoc = firestore.getTaskDoc(doc, task.workId, task.taskId)
                viewModelScope.launch {
                    if(firestore.mergeDocument(
                            taskDoc,
                            mapOf(
                                "checked" to newValue
                            )
                        )){
                        if(firestore.insertToArrayField(
                                firestore.getTrackingDoc(email),
                                "tasks",
                                taskDoc.path
                            )){
                            "Task checked changed".logAny()
                        }
                    }
                }
            }
        }
    }

    fun deleteWork(boardId: String, work: Work) {
        cardDoc?.let { doc->
            val workDoc = firestore.getWorkDoc(doc, work.workId)
            viewModelScope.launch {
                if(firestore.deleteDocument(
                        workDoc,
                )){
                    if(repository.workDao.deleteOne(work) > 0){
                        repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                            val activityId = "activity_${System.currentTimeMillis()}"
                            val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                            val message = MessageMaker.getDelWorkMessage(work.workName, _cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                            val remoteActivity = RemoteActivity(
                                activityId,
                                email,
                                boardId,
                                _cardId,
                                message,
                                false,
                                Activity.TYPE_ACTION,
                                System.currentTimeMillis()
                            )
                            firestore.addDocument(activityDoc, remoteActivity)

                            listMember.forEach { mem->
                                if(firestore.insertToArrayField(
                                        firestore.getTrackingDoc(mem.email),
                                        "delWorks",
                                        workDoc.path
                                    )){
                                    repository.taskDao.getTasksByWorkIdNoFlow(work.workId).forEach { task ->
                                        deleteTask(boardId, task, false)
                                    }
                                }
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "activities",
                                    activityDoc.path
                                )
                            }
                            postMessage("Delete work successfully")
                        }
                    }
                }
            }
        }
    }

    fun deleteActivity(workspaceId: String, boardId: String, activity: Activity) {
        val activityDoc = firestore.getActivityDoc(workspaceId, boardId, activity.activityId)
        viewModelScope.launch {
            if(firestore.deleteDocument(
                    activityDoc,
                )){
                if(repository.activityDao.deleteOne(activity) > 0){
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc1 = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getDelCommendMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            _cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc1, remoteActivity)

                        listMember.forEach { mem->
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "delActivities",
                                activityDoc.path
                            )
                            firestore.insertToArrayField(
                                firestore.getTrackingDoc(mem.email),
                                "activities",
                                activityDoc1.path
                            )
                        }
                        postMessage("Delete comment successfully")
                    }
                }
            }
        }
    }

    fun deleteAttachment(boardId: String, attachment: Attachment) {
        cardDoc?.let { doc->
            val attachmentDoc = firestore.getAttachmentDoc(doc, attachment.attachmentId)
            viewModelScope.launch {
                if(firestore.deleteDocument(
                    attachmentDoc,
                )){
                    if(repository.attachmentDao.deleteOne(attachment) > 0){
                        repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                            val activityId = "activity_${System.currentTimeMillis()}"
                            val activityDoc1 = firestore.getActivityDoc(boardDoc, activityId)
                            val message = MessageMaker.getDelAttachmentMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                            val remoteActivity = RemoteActivity(
                                activityId,
                                email,
                                boardId,
                                _cardId,
                                message,
                                false,
                                Activity.TYPE_ACTION,
                                System.currentTimeMillis()
                            )
                            firestore.addDocument(activityDoc1, remoteActivity)

                            listMember.forEach { mem->
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "delAttachments",
                                    attachmentDoc.path
                                )
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "activities",
                                    activityDoc1.path
                                )
                            }
                            postMessage("Delete attachment successfully")
                        }
                    }
                }
            }
        }
    }

    fun deleteThisCard(
        workspaceId: String,
        boardId: String,
        doOnFailed: () -> Unit,
        doOnSuccess: () -> Unit
    ){
        cardDoc?.let { doc->
            card.value?.let { c->
                viewModelScope.launch {
                    if(firestore.deleteDocument(
                        doc,
                    )){
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getDelCardMessage(_cardId, card.value?.cardName.toString(), boardId, board?.boardName.toString())
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            boardId,
                            _cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)

                        repository.activityDao.getActivityByCardId(c.cardId).forEach {
                            deleteActivity(workspaceId, boardId, it)
                        }
                        repository.attachmentDao.getAllByCardIdNoFlow(c.cardId).forEach {
                            deleteAttachment(boardId, it)
                        }
                        repository.workDao.getWorksByCardId(c.cardId).forEach {
                            deleteWork(boardId, it)
                        }
                        if(repository.cardDao.deleteOne(c) > 0){
                            repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                                listMember.forEach { mem->
                                    firestore.insertToArrayField(
                                        firestore.getTrackingDoc(mem.email),
                                        "delCards",
                                        doc.path
                                    )
                                    firestore.insertToArrayField(
                                        firestore.getTrackingDoc(mem.email),
                                        "activities",
                                        activityDoc.path
                                    )
                                }
                                doOnSuccess()
                            }
                        }
                    }
                    doOnFailed()
                }
            }?: doOnFailed()
        } ?: doOnFailed()
    }
}