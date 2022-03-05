package com.tnh.mollert.boardDetail

import android.content.ContentResolver
import android.util.Patterns
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentReference
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.compound.BoardWithLists
import com.tnh.mollert.datasource.local.compound.MemberAndActivity
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberCardRel
import com.tnh.mollert.datasource.remote.model.*
import com.tnh.mollert.home.CreateBoardDialog
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardDetailFragmentViewModel @Inject constructor(
    private val firestore: FirestoreHelper,
    private val repository: AppRepository,
    private val storage: StorageHelper
): BaseViewModel() {
    private val boardCardHelper = BoardCardHelper(repository, firestore)
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

    var cardAchieved: LiveData<kotlin.collections.List<Card>> = MutableLiveData(null)

    var memberAndActivity: LiveData<kotlin.collections.List<MemberAndActivity>> = MutableLiveData(null)
    var boardDoc: DocumentReference? = null
    private set

    fun setBoardDoc(workspaceId: String, boardId: String){
        boardDoc = firestore.getBoardDoc(workspaceId, boardId)
    }

    fun getAllList(boardId: String){
        boardWithLists = repository.appDao.getBoardWithLists(boardId).asLiveData()
        memberAndActivity = repository.appDao.getMemberAndActivityByBoardIdFlow(boardId).asLiveData()
        cardAchieved = repository.cardDao.getCardsWithBoardId(boardId, Card.STATUS_ACHIEVED).asLiveData()
        viewModelScope.launch {
            repository.memberDao.getBoardOwner(boardId)?.let { member->
                UserWrapper.getInstance()?.currentUserEmail?.let { email->
                    if(member.email == email){
                        isOwner = true
                    }
                }
            }
        }
    }

    suspend fun searchCard(search: String, boardId: String): kotlin.collections.List<Card>{
        return repository.cardDao.searchCardInBoard("%$search%", boardId)
    }

    fun getConcatList(list: kotlin.collections.List<List>): kotlin.collections.List<List>{
        return list.sortedBy { it.listName } + List("null",
            "",
            "",
            "",
            list.size)
    }

    val boardBackground = Transformations.map(boardWithLists){
        it?.board?.background ?: ""
    }

    fun changeBoardBackground(
        workspaceId: String,
        boardId: String,
        contentResolver: ContentResolver,
        uri: String,
        backgroundMode: String,
        onSuccess: () -> Unit
    ){
        showProgress()
        val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
        viewModelScope.launch {
            try{
                var bg = uri
                if(backgroundMode == CreateBoardDialog.BACKGROUND_MODE_CUSTOM){
                    storage.uploadBackgroundImage(workspaceId, boardId, contentResolver, uri.toUri())?.let { url->
                        bg = url.toString()
                    }
                }
                if(firestore.mergeDocument(
                        boardDoc,
                        mapOf(
                            "boardBackground" to bg
                        )
                    )){
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                    UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                        val message = MessageMaker.getChangeBoardBackgroundMessage(boardId, boardWithLists.value?.board?.boardName ?: "")
                        val remoteActivity = RemoteActivity(
                            activityId,
                            member.email,
                            boardId,
                            null,
                            message,
                            false,
                            Activity.TYPE_INFO,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)
                    }
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            val tracking = firestore.getTrackingDoc(mem.email)
                            firestore.insertToArrayField(tracking, "boards", boardDoc.path)
                            firestore.insertToArrayField(tracking, "activities", activityDoc.path)
                        }
                    }
                    hideProgress()
                    onSuccess()
                    postMessage("Change background successfully")
                }
            }catch (e: Exception){
                trace(e)
                hideProgress()
                postMessage("Something went wrong")
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
                val activityId = "activity_${System.currentTimeMillis()}"
                val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                    val message = MessageMaker.getChangeBoardDescMessage(boardId, boardWithLists.value?.board?.boardName ?: "")
                    val remoteActivity = RemoteActivity(
                        activityId,
                        member.email,
                        boardId,
                        null,
                        message,
                        false,
                        Activity.TYPE_INFO,
                        System.currentTimeMillis()
                    )
                    firestore.addDocument(activityDoc, remoteActivity)
                }
                repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                    listMember.forEach { mem->
                        val tracking = firestore.getTrackingDoc(mem.email)
                        firestore.insertToArrayField(tracking, "boards", boardDoc.path)
                        firestore.insertToArrayField(tracking, "activities", activityDoc.path)
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
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc!!, activityId)
                    UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                        val message = MessageMaker.getCreateListMessage(boardId,boardWithLists.value?.board?.boardName ?: "", listName)
                        val remoteActivity = RemoteActivity(
                            activityId,
                            member.email,
                            boardId,
                            null,
                            message,
                            false,
                            Activity.TYPE_INFO,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)
                    }
                    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            val tracking = firestore.getTrackingDoc(mem.email)
                            firestore.insertToArrayField(tracking, "lists", listDoc.path)
                            firestore.insertToArrayField(tracking, "activities", activityDoc.path)
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
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc!!, activityId)
                    UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                        val message = MessageMaker.getCreateCardMessage(boardId, boardWithLists.value?.board?.boardName ?: "", cardId, cardName)
                        val remoteActivity = RemoteActivity(
                            activityId,
                            member.email,
                            boardId,
                            cardId,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)
                    }

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
                            firestore.insertToArrayField(
                                tracking,
                                "activities",
                                activityDoc.path
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
            if(prefManager.getString("$email+$boardId").isEmpty()){
                "Fetching all board content".logAny()
                viewModelScope.launch {
                    fetchAllLabels(workspaceId, boardId)
                    fetchAllList(workspaceId, boardId)
                    fetchAllActivity(workspaceId, boardId)
                    prefManager.putString("$email+$boardId", "synced")
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

    fun leaveBoard(workspaceId: String, boardId: String, prefManager: PrefManager, onSuccess: ()-> Unit){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            val memberDoc = firestore.getMemberDoc(email)
            viewModelScope.launch {
                if(firestore.removeFromArrayField(
                    firestore.getBoardDoc(workspaceId, boardId),
                    "members",
                    RemoteMemberRef(
                        email,
                        memberDoc.path,
                        MemberBoardRel.ROLE_MEMBER
                    )
                )){
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc!!, activityId)
                    UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                        val message = MessageMaker.getLeaveBoardMessage(boardId, boardWithLists.value?.board?.boardName ?: "")
                        val remoteActivity = RemoteActivity(
                            activityId,
                            member.email,
                            boardId,
                            null,
                            message,
                            false,
                            Activity.TYPE_ACTION,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)
                    }

                    repository.memberBoardDao.getRelByEmailAndBoardId(email, boardId)?.let {
                        repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                            listMember.forEach { mem->
                                val tracking = firestore.getTrackingDoc(mem.email)
                                firestore.insertToArrayField(
                                    tracking,
                                    "leaveBoards",
                                    firestore.getBoardDoc(workspaceId, boardId).path
                                )
                                firestore.insertToArrayField(
                                    tracking,
                                    "activities",
                                    activityDoc.path
                                )
                            }
                        }
                        prefManager.putString("$email+$boardId", "")
                        onSuccess()
                    }
                }
            }
        }
    }

    fun closeBoard(workspaceId: String, boardId: String, prefManager: PrefManager){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
            viewModelScope.launch {
                if(firestore.mergeDocument(
                        boardDoc,
                        mapOf("boardStatus" to Board.STATUS_CLOSED)
                )){
                    prefManager.putString("$email+$boardId", "")
                    // notify all members in workspace
                    repository.appDao.getWorkspaceWithMembersNoFlow(workspaceId)?.members?.let { listMember->
                        listMember.forEach { mem->
                            val tracking = firestore.getTrackingDoc(mem.email)
                            firestore.insertToArrayField(
                                tracking,
                                "closeBoards",
                                boardDoc.path
                            )
                        }
                        postMessage("Reopen board successfully")
                    }
                }
            }
        }
    }

    fun achieveList(listId: String){
        boardWithLists.value?.board?.let { b->
            viewModelScope.launch {
                showProgress()
                val cards = repository.cardDao.getActiveCardsByListId(listId)
                cards.forEach {
                    boardCardHelper.achieveCard(
                        b,
                        it,
                        email
                    ){}
                }
                if(cards.isNotEmpty()){
                    postMessage("Cards are achieved")
                }
                hideProgress()
            }
        }
    }

    fun changeVisibility(boardId: String, newVisibility: String){
        boardDoc?.let { doc->
            if(boardWithLists.value?.board?.boardVisibility != newVisibility){
                viewModelScope.launch {
                    if(firestore.mergeDocument(
                            doc,
                            mapOf("boardVisibility" to Board.STATUS_CLOSED)
                        )){
                        repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                            val activityId = "activity_${System.currentTimeMillis()}"
                            val activityDoc = firestore.getActivityDoc(boardDoc!!, activityId)
                            UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                                val message = MessageMaker.getChangeBoardVisMessage(boardId, boardWithLists.value?.board?.boardName ?: "", newVisibility)
                                val remoteActivity = RemoteActivity(
                                    activityId,
                                    member.email,
                                    boardId,
                                    null,
                                    message,
                                    false,
                                    Activity.TYPE_ACTION,
                                    System.currentTimeMillis()
                                )
                                firestore.addDocument(activityDoc, remoteActivity)
                            }

                            listMember.forEach { mem->
                                val tracking = firestore.getTrackingDoc(mem.email)
                                firestore.insertToArrayField(
                                    tracking,
                                    "boards",
                                    mapOf(
                                        "what" to "all",
                                        "ref" to doc.path
                                    )
                                )
                                firestore.insertToArrayField(
                                    tracking,
                                    "activities",
                                    activityDoc.path
                                )
                            }
                            postMessage("Changed to $newVisibility")
                        }
                    }
                }
            }
        }
    }

    fun inviteMemberToBoard(otherEmail: String, workspaceId: String){
        viewModelScope.launch {
            UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                val email = member.email
                if (email == otherEmail) {
                    postMessage("You can not invite yourself")
                    cancel()
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(otherEmail).matches()) {
                    postMessage("Invalid email address")
                    cancel()
                }
                boardWithLists.value?.board?.let { b->
                    firestore.simpleGetDocumentModel<RemoteMember>(firestore.getMemberDoc(otherEmail))?.toMember()?.let { m ->
                        val id = "invitation_${b.boardId}_"
                        val remoteActivity = RemoteActivity(
                            id + System.currentTimeMillis(),
                            email,
                            null,
                            null,
                            "",
                            false,
                            Activity.TYPE_INFO,
                            System.currentTimeMillis()
                        )
                        remoteActivity.message = MessageMaker.getBoardInvitationSenderMessage(
                            b.boardId,
                            b.boardName,
                            m.email,
                            m.name
                        )
                        sendNotification(email, remoteActivity)
                        remoteActivity.activityId = id + System.currentTimeMillis()
                        remoteActivity.actor = otherEmail
                        remoteActivity.activityType = Activity.TYPE_INVITATION_BOARD
                        remoteActivity.message = MessageMaker.getBoardInvitationReceiverMessage(
                            b.boardId,
                            b.boardName,
                            workspaceId,
                            "",
                            member.email,
                            member.name
                        )
                        sendNotification(otherEmail, remoteActivity)
                        postMessage("Sent invitation successfully")
                    } ?: postMessage("No such member exist")
                }
            }
        }
    }

    private suspend fun sendNotification(email: String, remoteActivity: RemoteActivity){
        firestore.insertToArrayField(
            firestore.getTrackingDoc(email),
            "invitations",
            remoteActivity
        )
    }

    fun deleteList(list: List){
        boardWithLists.value?.board?.let { b->
            viewModelScope.launch {
                showProgress()
                val cards = repository.cardDao.getActiveCardsByListId(list.listId)
                cards.forEach {
                    boardCardHelper.deleteThisCard(
                        b,
                        it,
                        email,
                        this@BoardDetailFragmentViewModel,
                    ){}
                }
                val doc = firestore.getListDoc(b.workspaceId, b.boardId, list.listId)
                if (firestore.deleteDocument(
                        doc,
                    )) {
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(firestore.getBoardDoc(b.workspaceId, b.boardId), activityId)
                    val message = MessageMaker.getDelListMessage(
                        list.listName,
                        b.boardId,
                        b.boardName
                    )
                    val remoteActivity = RemoteActivity(
                        activityId,
                        email,
                        b.boardId,
                        null,
                        message,
                        false,
                        Activity.TYPE_INFO,
                        System.currentTimeMillis()
                    )
                    firestore.addDocument(activityDoc, remoteActivity)

                    if (repository.listDao.deleteOne(list) > 0) {
                        repository.appDao.getBoardWithMembers(b.boardId)?.members?.let { listMember ->
                            listMember.forEach { mem ->
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "delLists",
                                    doc.path
                                )
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
                                    "activities",
                                    activityDoc.path
                                )
                            }
                        }
                    }
                }
                postMessage("Delete successfully")
                hideProgress()
            }
        }

    }

    fun changeBoardName(name: String, workspaceId: String, boardId: String) {
        val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
        viewModelScope.launch {
            val data = mapOf(
                "boardName" to name
            )
            if(firestore.mergeDocument(boardDoc, data)){
                val boardWithMember = repository.appDao.getBoardWithMembers(boardId)
                boardWithMember?.members?.let { listMember ->
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                    val message = MessageMaker.getChangeBoardNameMessage(
                        boardId,
                        boardWithMember.board.boardName,
                        name
                    )
                    val remoteActivity = RemoteActivity(
                        activityId,
                        email,
                        boardId,
                        null,
                        message,
                        false,
                        Activity.TYPE_INFO,
                        System.currentTimeMillis()
                    )
                    firestore.addDocument(activityDoc, remoteActivity)

                    listMember.forEach { mem ->
                        firestore.insertToArrayField(
                            firestore.getTrackingDoc(mem.email),
                            "boards",
                            mapOf(
                                "what" to "info",
                                "ref" to boardDoc.path
                            )
                        )
                        firestore.insertToArrayField(
                            firestore.getTrackingDoc(mem.email),
                            "activities",
                            activityDoc.path
                        )
                    }
                }
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
        val listDoc = firestore.getListDoc(workspaceId, boardId, list.listId)
        viewModelScope.launch {
            val data = mapOf(
                "name" to name
            )
            if(firestore.mergeDocument(listDoc, data)){
                val boardWithMember = repository.appDao.getBoardWithMembers(boardId)
                boardWithMember?.members?.let { listMember ->
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(listDoc, activityId)
                    val message = MessageMaker.getChangeListNameMessage(
                        boardId,
                        boardWithMember.board.boardName,
                        list.listName,
                        name
                    )
                    val remoteActivity = RemoteActivity(
                        activityId,
                        email,
                        boardId,
                        null,
                        message,
                        false,
                        Activity.TYPE_INFO,
                        System.currentTimeMillis()
                    )
                    firestore.addDocument(activityDoc, remoteActivity)

                    listMember.forEach { mem ->
                        firestore.insertToArrayField(
                            firestore.getTrackingDoc(mem.email),
                            "lists",
                            listDoc.path
                        )
                        firestore.insertToArrayField(
                            firestore.getTrackingDoc(mem.email),
                            "activities",
                            activityDoc.path
                        )
                    }
                }
                postMessage("Change list name successfully")
            }
        }
    }

}
