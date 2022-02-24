package com.tnh.mollert.cardDetail

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.*
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

    fun setCardDoc(workspaceId: String, boardId: String, listId: String, cardId: String){
        cardDoc = firestore.getCardDoc(workspaceId, boardId, listId, cardId)
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

    fun achieveCard(){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                        doc,
                        mapOf(
                            "status" to Card.STATUS_ACHIEVED,
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
                        postMessage("Card achieved")
                    }
                }
            }
        }
    }

    fun activateCard(){
        cardDoc?.let { doc->
            viewModelScope.launch {
                if(firestore.mergeDocument(
                        doc,
                        mapOf(
                            "status" to Card.STATUS_ACTIVE,
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
                        postMessage("Card activated")
                    }
                }
            }
        }
    }

    fun joinCard(){
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
                    if(firestore.insertToArrayField(
                            firestore.getTrackingDoc(email),
                            "cards",
                            mapOf(
                                "what" to "member",
                                "ref" to doc.path
                            )
                        )){
                        postMessage("Join successfully")
                    }
                }
            }
        }
    }

    fun leaveCard(cardId: String){
        cardDoc?.let { doc->
            viewModelScope.launch {
                repository.memberCarDao.getRelByEmailAndCardId(email, cardId)?.let { memberCardRel ->
                    if(firestore.removeFromArrayField(
                            doc,
                            "members",
                            memberCardRel.toRemote(firestore.getMemberDoc(email).path)
                        )){
                        if(firestore.insertToArrayField(
                                firestore.getTrackingDoc(email),
                                "cards",
                                mapOf(
                                    "what" to "member",
                                    "ref" to doc.path
                                )
                            )){
                            postMessage("Leave successfully")
                        }
                    }
                }
            }
        }
    }


    fun addWork(cardId: String, workName: String) {
        val workId = "${workName}_${System.currentTimeMillis()}"
        cardDoc?.let { doc->
            val workDoc = firestore.getWorkDoc(doc, workId)
            val remoteWork = RemoteWork(workId, workName, workDoc.path, cardId)
            viewModelScope.launch {
                if(firestore.addDocument(
                    workDoc,
                    remoteWork
                )){
                    if(firestore.insertToArrayField(
                        firestore.getTrackingDoc(email),
                        "works",
                        workDoc.path
                    )){
                        postMessage("Add work successfully")
                    }
                }
            }
        }
    }

    fun addTask(workId: String, taskName: String){
        val taskId = "${taskName}_${System.currentTimeMillis()}"
        cardDoc?.let { doc->
            val taskDoc = firestore.getTaskDoc(doc, workId, taskId)
            val remoteTask = RemoteTask(taskId, taskName, workId)
            viewModelScope.launch {
                if(firestore.addDocument(
                    taskDoc,
                    remoteTask
                )){
                    if(firestore.insertToArrayField(
                            firestore.getTrackingDoc(email),
                            "tasks",
                            taskDoc.path
                        )){
                        postMessage("Add task successfully")
                    }
                }
            }
        }
    }

    fun deleteTask(task: Task, isShow: Boolean = true){
        cardDoc?.let { doc->
            val taskDoc = firestore.getTaskDoc(doc, task.workId, task.taskId)
            viewModelScope.launch {
                if(firestore.deleteDocument(
                        taskDoc,
                )){
                    if(repository.taskDao.deleteOne(task) > 0){
                        if(firestore.insertToArrayField(
                                firestore.getTrackingDoc(email),
                                "delTasks",
                                taskDoc.path
                        )){
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

    fun deleteWork(work: Work) {
        cardDoc?.let { doc->
            val workDoc = firestore.getWorkDoc(doc, work.workId)
            viewModelScope.launch {
                if(firestore.deleteDocument(
                        workDoc,
                )){
                    if(repository.workDao.deleteOne(work) > 0){
                        if(firestore.insertToArrayField(
                            firestore.getTrackingDoc(email),
                            "delWorks",
                            workDoc.path
                        )){
                            repository.taskDao.getTasksByWorkIdNoFlow(work.workId).forEach { task ->
                                deleteTask(task, false)
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
                    if(firestore.insertToArrayField(
                        firestore.getTrackingDoc(email),
                        "delActivities",
                        activityDoc.path
                    )){
                        postMessage("Delete comment successfully")
                    }
                }
            }
        }
    }

    fun deleteAttachment(attachment: Attachment) {
        cardDoc?.let { doc->
            val attachmentDoc = firestore.getAttachmentDoc(doc, attachment.attachmentId)
            viewModelScope.launch {
                if(firestore.deleteDocument(
                    attachmentDoc,
                )){
                    if(repository.attachmentDao.deleteOne(attachment) > 0){
                        if(firestore.insertToArrayField(
                            firestore.getTrackingDoc(email),
                            "delAttachments",
                            attachmentDoc.path
                        )){
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
                        repository.activityDao.getActivityByCardId(c.cardId).forEach {
                            deleteActivity(workspaceId, boardId, it)
                        }
                        repository.attachmentDao.getAllByCardIdNoFlow(c.cardId).forEach {
                            deleteAttachment(it)
                        }
                        repository.workDao.getWorksByCardId(c.cardId).forEach {
                            deleteWork(it)
                        }
                        if(repository.cardDao.deleteOne(c) > 0){
                            if(firestore.insertToArrayField(
                                firestore.getTrackingDoc(email),
                                "delCards",
                                doc.path
                            )){
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