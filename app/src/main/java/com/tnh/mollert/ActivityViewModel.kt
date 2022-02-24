package com.tnh.mollert

import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberCardRel
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
    private var eventChangedListener: ListenerRegistration? = null

    fun registerRemoteEvent(){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            eventChangedListener = firestore.listenDocument(
                firestore.getTrackingDoc(email),
                {
                    trace(it)
                }
            ){ snap->
                snap?.data?.let { map->
                    registerWorkspace(email, map)
                    registerBoard(email, map)
                    registerInvitation(email, map)
                    registerInfoChanged(email, map)
                    registerList(email, map)
                    registerCard(email, map)
                    registerLabel(email, map)
                    registerAttachment(email, map)
                    registerActivity(email, map)
                    registerWork(email, map)
                    registerTask(email, map)

                    registerDelLabel(email, map)
                    registerDelTask(email, map)
                    registerDelActivity(email, map)
                    registerDelWork(email, map)
                    registerDelAttachment(email, map)
                    registerDelCard(email, map)
                    registerLeaveBoard(email, map)
                }
            }
        }
    }

    private fun registerLeaveBoard(email: String, map: Map<String, Any>) {
        (map["leaveBoards"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Leaving board $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        firestore.simpleGetDocumentModel<RemoteBoard>(doc)?.let { remoteBoard->
                            remoteBoard.members?.let { listMember->
                                remoteBoard.boardId?.let { boardId->
                                    repository.memberBoardDao.getRelsByBoardId(boardId).forEach {
                                        repository.memberBoardDao.deleteOne(it)
                                    }
                                    listMember.forEach { rmr->
                                        rmr.toMemberBoardReal(remoteBoard.boardId)?.let {
                                            repository.memberBoardDao.insertOne(it)
                                        }
                                    }
                                }
                            }
                        }
                        firestore.removeFromArrayField(firestore.getTrackingDoc(email), "leaveBoards", ref)
                    }
                }
            }
        }
    }

    private fun registerDelCard(email: String, map: Map<String, Any>) {
        (map["delCards"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Deleting card $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        val cardId = doc.id
                        repository.cardDao.getCardByIdNoFlow(cardId)?.let { card ->
                            repository.cardDao.deleteOne(card)
                        }
                        firestore.removeFromArrayField(firestore.getTrackingDoc(email), "delCards", ref)
                    }
                }
            }
        }
    }

    private fun registerDelAttachment(email: String, map: Map<String, Any>) {
        (map["delAttachments"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Deleting attachment $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        val attachmentId = doc.id
                        repository.attachmentDao.getAttachmentById(attachmentId)?.let { attachment ->
                            repository.attachmentDao.deleteOne(attachment)
                        }
                        firestore.removeFromArrayField(firestore.getTrackingDoc(email), "delAttachments", ref)
                    }
                }
            }
        }
    }

    private fun registerDelWork(email: String, map: Map<String, Any>) {
        (map["delWorks"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Deleting work $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        val workId = doc.id
                        repository.workDao.getWorkById(workId)?.let { work ->
                            repository.workDao.deleteOne(work)
                        }
                        firestore.removeFromArrayField(firestore.getTrackingDoc(email), "delWorks", ref)
                    }
                }
            }
        }
    }

    private fun registerDelActivity(email: String, map: Map<String, Any>) {
        (map["delActivities"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Deleting activity $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        val activityId = doc.id
                        repository.activityDao.getActivityById(activityId)?.let { activity ->
                            repository.activityDao.deleteOne(activity)
                        }
                        firestore.removeFromArrayField(firestore.getTrackingDoc(email), "delActivities", ref)
                    }
                }
            }
        }
    }

    private fun registerWork(email: String, map: Map<String, Any>) {
        (map["works"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Loading work $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        firestore.simpleGetDocumentModel<RemoteWork>(doc)?.let { remoteWork ->
                            remoteWork.toModel()?.let { work->
                                repository.workDao.insertOne(work)
                                firestore.removeFromArrayField(firestore.getTrackingDoc(email), "works", ref)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun registerActivity(email: String, map: Map<String, Any>) {
        (map["activities"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Loading activity $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        firestore.simpleGetDocumentModel<RemoteActivity>(doc)?.let { remoteActivity ->
                            remoteActivity.toModel()?.let { activity->
                                repository.activityDao.insertOne(activity)
                                firestore.removeFromArrayField(firestore.getTrackingDoc(email), "activities", ref)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun registerTask(email: String, map: Map<String, Any>) {
        (map["tasks"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Loading task $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        firestore.simpleGetDocumentModel<RemoteTask>(doc)?.let { remoteTask ->
                            remoteTask.toModel()?.let { task->
                                repository.taskDao.insertOne(task)
                                firestore.removeFromArrayField(firestore.getTrackingDoc(email), "tasks", ref)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun registerDelLabel(email: String, map: Map<String, Any>) {
        (map["delLabels"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Deleting labels $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        val labelId = doc.id
                        repository.labelDao.getLabelById(labelId)?.let { label ->
                            repository.labelDao.deleteOne(label)
                            repository.cardLabelDao.getRelByLabelId(labelId).forEach {
                                repository.cardLabelDao.deleteAll(it)
                            }
                        }
                        firestore.removeFromArrayField(firestore.getTrackingDoc(email), "delLabels", ref)
                    }
                }
            }
        }
    }

    private fun registerDelTask(email: String, map: Map<String, Any>) {
        (map["delTasks"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Deleting tasks $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                            val taskId = doc.id
                        repository.taskDao.getTaskByTaskId(taskId)?.let { task ->
                            repository.taskDao.deleteOne(task)
                        }
                        firestore.removeFromArrayField(firestore.getTrackingDoc(email), "delTasks", ref)
                    }
                }
            }
        }
    }

    private fun registerLabel(email: String, map: Map<String, Any>) {
        (map["labels"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Loading labels $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        firestore.simpleGetDocumentModel<RemoteLabel>(doc)?.let { remoteLabel ->
                            remoteLabel.toLabel()?.let { label->
                                repository.labelDao.insertOne(label)
                                firestore.removeFromArrayField(firestore.getTrackingDoc(email), "labels", ref)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun registerAttachment(email: String, map: Map<String, Any>) {
        (map["attachments"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    "Loading attachment $ref".logAny()
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        firestore.simpleGetDocumentModel<RemoteAttachment>(doc)?.let { remoteAttachment ->
                            remoteAttachment.toModel().let { attachment->
                                repository.attachmentDao.insertOne(attachment)
                                firestore.removeFromArrayField(firestore.getTrackingDoc(email), "attachments", ref)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun registerCard(email: String, map: Map<String, Any>) {
        (map["cards"] as List<Map<String, String>>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { entry->
                    "Loading card $entry".logAny()
                    val what = entry.getOrElse("what"){"info"}
                    val ref = entry["ref"]
                    viewModelScope.launch {
                        ref?.let {
                            val doc = firestore.getDocRef(ref)
                            firestore.simpleGetDocumentModel<RemoteCard>(doc)?.let { remoteCard ->
                                when(what){
                                    "info"->{
                                        remoteCard.toModel()?.let { card->
                                            repository.cardDao.insertOne(card)
                                            firestore.removeFromArrayField(firestore.getTrackingDoc(email), "cards", entry)
                                        }
                                    }
                                    "label"->{
                                        remoteCard.cardId?.let {
                                            addCardLabelRel(remoteCard.cardId, remoteCard.labels)
                                            firestore.removeFromArrayField(firestore.getTrackingDoc(email), "cards", entry)
                                        }
                                    }
                                    "member"->{
                                        remoteCard.cardId?.let {
                                            addCardMemberRel(remoteCard.cardId, remoteCard.members)
                                            firestore.removeFromArrayField(firestore.getTrackingDoc(email), "cards", entry)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun addCardMemberRel(cardId: String, remoteCard: List<RemoteMemberRef>){
        repository.memberCarDao.getRelByCardId(cardId).forEach {
            repository.memberCarDao.deleteOne(it)
        }
        remoteCard.forEach { remoteMemberRef ->
            remoteMemberRef.email?.let { e->
                repository.memberCarDao.insertOne(MemberCardRel(
                    e,
                    cardId,
                    remoteMemberRef.role
                ))
            }
        }
    }

    private suspend fun addCardLabelRel(cardId: String, remoteCard: List<RemoteLabelRef>){
        repository.cardLabelDao.getRelByCardId(cardId).forEach {
            repository.cardLabelDao.deleteOne(it)
        }
        remoteCard.forEach { remoteLabelRef ->
            remoteLabelRef.labelId?.let {
                repository.cardLabelDao.insertOne(CardLabelRel(cardId, it))
            }
        }
    }

    private fun registerList(email: String, map: Map<String, Any>) {
        (map["lists"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach { ref->
                    viewModelScope.launch {
                        val doc = firestore.getDocRef(ref)
                        firestore.simpleGetDocumentModel<RemoteList>(doc)?.let { remoteList ->
                            remoteList.toModel()?.let { l->
                                repository.listDao.insertOne(l)
                                firestore.removeFromArrayField(firestore.getTrackingDoc(email), "lists", ref)
                            }
                        }
                    }
                }
            }else{
                // we don't need to fetch all lists at this time
            }
        }
    }

    private fun registerInfoChanged(email: String, map: Map<String, Any>) {
        (map["info"] as List<String>?)?.let { listRef->
            if(listRef.isNotEmpty()){
                listRef.forEach {
                    viewModelScope.launch {
                        UserWrapper.getInstance()?.fetchMember(email)?.let { member ->
                            "Updated user info: $member".logAny()
                            firestore.removeFromArrayField(firestore.getTrackingDoc(email), "info", it)
                        }
                    }
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

                // moved to home fragment
//                viewModelScope.launch {
//                    repository.workspaceDao.countOne()?.let {
//                        if(it == 0){
//                            reloadWorkspaceFromRemote(email)
//                        }
//                    }
//                }
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
                // moved to home fragment
//                viewModelScope.launch {
//                    repository.boardDao.countOne()?.let {
//                        if(it == 0){
//                            "Reloading all boards from remote".logAny()
//                            reloadBoardFromRemote(email)
//                        }
//                    }
//                }
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
                                it.logAny()
                                repository.activityDao.insertOne(it)
                                firestore.removeFromArrayField(firestore.getTrackingDoc(email), "invitations", remoteActivity)
                            }
                        }
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
                            saveMemberAndRelationFromRemote(rmr.ref, model.boardId, rmr.role)
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveMemberAndRelationFromRemote(ref: String, boardId: String, role: String){
        firestore.simpleGetDocumentModel<RemoteMember>(
            firestore.getDocRef(ref)
        )?.let { rm->
            rm.toMember()?.let {
                repository.memberDao.insertOne(it)
                repository.memberBoardDao.insertOne(MemberBoardRel(it.email, boardId, role))
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
                            saveMemberAndRelationFromRemote(rmr.ref, board.boardId, rmr.role)
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
                saveMemberWorkspaceRelation(it.members, model.workspaceId)
                repository.memberWorkspaceDao.insertOne(MemberWorkspaceRel(email, model.workspaceId))
                saveAllBoardFromRemote(model.workspaceId)
            }
        }
    }

    private suspend fun saveMemberWorkspaceRelation(list: List<RemoteMemberRef>, workspaceId: String){
        list.forEach { rmr->
            rmr.email?.let { email->
                repository.memberWorkspaceDao.insertOne(MemberWorkspaceRel(email, workspaceId, rmr.role))
            }
        }
    }

    private fun unregisterRemoteEvent(){
        eventChangedListener?.remove()
        eventChangedListener = null
    }

    override fun onCleared() {
        super.onCleared()
        unregisterRemoteEvent()
    }
}