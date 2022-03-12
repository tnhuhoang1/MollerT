package com.tnh.mollert.datasource

import android.content.ContentResolver
import android.util.Patterns
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.tnh.mollert.boardDetail.BoardCardHelper
import com.tnh.mollert.datasource.local.compound.BoardWithLists
import com.tnh.mollert.datasource.local.compound.MemberWithWorkspaces
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberCardRel
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.datasource.remote.model.*
import com.tnh.mollert.home.CreateBoardDialog
import com.tnh.mollert.utils.*
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine

class AppRepository private constructor(
    val local: DataSource,
    val firestore: FirestoreAction,
    val storage: StorageFunction
) {

    suspend fun getBoardById(boardId: String): Board?{
        return local.boardDao.getBoardByIdNoFlow(boardId)
    }

    suspend fun searchBoard(text: String): List<Board>{
        return local.boardDao.searchBoard("%$text%")
    }

    fun getMemberWithWorkspaces(email: String): LiveData<MemberWithWorkspaces>{
        return local.appDao.getMemberWithWorkspaces(email).asLiveData()
    }

    suspend fun syncWorkspacesAndBoardsDataFirstTime(
        pref: PrefManager
    ){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            if(pref.getString("$email+sync+all").isEmpty()){
                coroutineScope {
                    "Syncing all workspaces and boards data".logAny()
                    reloadWorkspaceFromRemote(email)
                    pref.putString("$email+sync+all", "synced")
                    "Synced".logAny()
                }
            }
        }
    }

    private suspend fun reloadWorkspaceFromRemote(email: String) {
        "Reloading all workspaces from remote".logAny()
        firestore.simpleGetDocumentModel(
            RemoteMember::class.java,
            firestore.getMemberDoc(email)
        )?.let { rm->
            rm.workspaces?.forEach { ws->
                ws.ref?.let { ref->
                    saveWorkspaceFromRemote(email, ref)
                }
            }
        }
    }

    private suspend fun saveWorkspaceFromRemote(email: String, ref: String){
        firestore.simpleGetDocumentModel(
            RemoteWorkspace::class.java,
            firestore.getDocRef(ref)
        )?.let {
            it.toModel()?.let { model->
                local.workspaceDao.insertOne(model)
                saveMemberWorkspaceRelation(it.members, model.workspaceId)
                saveAllBoardFromRemote(model.workspaceId)
            }
        }
    }

    private suspend fun saveMemberWorkspaceRelation(list: List<RemoteMemberRef>, workspaceId: String){
        list.forEach { rmr->
            rmr.email?.let { email->
                local.memberWorkspaceDao.insertOne(MemberWorkspaceRel(email, workspaceId, rmr.role))
            }
        }
    }

    private suspend fun saveAllBoardFromRemote(workspaceId: String){
        firestore.getCol(firestore.getBoardCol(workspaceId))?.documentChanges?.forEach { docChange->
            val rb = docChange.document.toObject(RemoteBoard::class.java)
            rb.toModel()?.let { board->
                local.boardDao.insertOne(board)
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

    private suspend fun saveMemberAndRelationFromRemote(ref: String, boardId: String, role: String){
        firestore.simpleGetDocumentModel(
            RemoteMember::class.java,
            firestore.getDocRef(ref)
        )?.let { rm->
            rm.toMember()?.let {
                local.memberDao.insertOne(it)
                local.memberBoardDao.insertOne(MemberBoardRel(it.email, boardId, role))
            }
        }
    }

    fun getMemberWorkspaceDao() = local.memberWorkspaceDao

    private suspend fun sendInviteNotification(email: String, remoteActivity: RemoteActivity){
        firestore.insertToArrayField(
            firestore.getTrackingDoc(email),
            "invitations",
            remoteActivity
        )
    }

    suspend inline fun reopenBoard(workspaceId: String, boardId: String, onSuccess: () -> Unit = {}){
        val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
        if(firestore.mergeDocument(
                boardDoc,
                mapOf("boardStatus" to Board.STATUS_OPEN)
        )){
            onSuccess()
            "reopening board".logAny()
            // notify all members in workspace
            local.appDao.getWorkspaceWithMembersNoFlow(workspaceId)?.members?.let { listMember->
                listMember.forEach { mem->
                    val tracking = firestore.getTrackingDoc(mem.email)
                    firestore.insertToArrayField(
                        tracking,
                        "closeBoards",
                        boardDoc.path
                    )
                }
            }
        }
    }

    suspend fun searchCard(search: String, boardId: String): List<Card>{
        return local.cardDao.searchCardInBoard("%$search%", boardId)
    }

    suspend fun changeBoardBackground(
        workspaceId: String,
        boardId: String,
        boardName: String,
        contentResolver: ContentResolver,
        uri: String,
        backgroundMode: String,
        onFailed: () -> Unit,
        onSuccess: () -> Unit
    ){
        val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
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
                    val message = MessageMaker.getChangeBoardBackgroundMessage(boardId, boardName)
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
                local.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                    listMember.forEach { mem->
                        val tracking = firestore.getTrackingDoc(mem.email)
                        firestore.insertToArrayField(tracking, "boards", mapOf(
                            "what" to "info",
                            "ref" to boardDoc.path
                        ))
                        firestore.insertToArrayField(tracking, "activities", activityDoc.path)
                    }
                }
                onSuccess()
            }
        }catch (e: Exception){
            trace(e)
            onFailed()
        }
    }

    suspend fun changeDescription(
        workspaceId: String,
        boardId: String,
        boardName: String,
        content: String,
        onSuccess: () -> Unit
    ){
        val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
        if(firestore.mergeDocument(
                boardDoc,
                mapOf(
                    "boardDesc" to content
                )
            )){
            val activityId = "activity_${System.currentTimeMillis()}"
            val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
            UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                val message = MessageMaker.getChangeBoardDescMessage(boardId, boardName)
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
            local.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                listMember.forEach { mem->
                    val tracking = firestore.getTrackingDoc(mem.email)
                    firestore.insertToArrayField(tracking, "boards", mapOf(
                        "what" to "info",
                        "ref" to boardDoc.path
                    ))
                    firestore.insertToArrayField(tracking, "activities", activityDoc.path)
                    onSuccess()
                }
            }
        }
    }

    suspend fun createBoard(
        email: String,
        workspace: Workspace,
        boardName: String,
        visibility: String,
        background: String,
        backgroundMode: String,
        contentResolver: ContentResolver,
        onFailed: () -> Unit,
        onSuccess: ()-> Unit
    ){
        val boardId = "${boardName}_${System.currentTimeMillis()}"
        val boardDoc = firestore.getBoardDoc(workspace.workspaceId, boardId)
        val remoteBoard = RemoteBoard(
            boardId,
            workspace.workspaceId,
            boardName,
            "",
            background,
            Board.STATUS_OPEN,
            visibility,
            listOf(RemoteMemberRef(email, firestore.getMemberDoc(email).path)),
            listOf()
        )
        if(backgroundMode == CreateBoardDialog.BACKGROUND_MODE_CUSTOM){
            try{
                storage.uploadBackgroundImage(workspace.workspaceId, boardId, contentResolver, background.toUri())?.let{
                    remoteBoard.boardBackground = it.toString()
                } ?: throw Exception("Failed")
            }catch (e: Exception){
                trace(e)
                onFailed()
                return
            }
        }

        if(firestore.addDocument(boardDoc, remoteBoard)){
            LabelPreset.getPresetLabelList(boardId).forEach { remoteLabel->
                val labelDoc = firestore.getLabelDoc(workspace.workspaceId, boardId, remoteLabel.labelId)
                if(firestore.addDocument(
                        labelDoc,
                        remoteLabel
                    )){
                    firestore.insertToArrayField(
                        firestore.getTrackingDoc(email),
                        "labels",
                        labelDoc.path
                    )
                }
            }
            val message = MessageMaker.getCreateBoardMessage(boardId, boardName)
            val activityId = "created_${System.currentTimeMillis()}"
            val activityDoc = firestore.getActivityDoc(workspace.workspaceId, boardId, activityId)
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
            if(firestore.addDocument(
                    activityDoc,
                    remoteActivity
                )){
                notifyBoardMember(local, firestore, boardId, "activities", activityDoc.path)
            }
            local.appDao.getWorkspaceWithMembersNoFlow(workspace.workspaceId)?.members?.let { members->
                "Notify to other members (${members.size}) about new board inserted".logAny()
                members.forEach {
                    val trackingLoc = firestore.getTrackingDoc(it.email)
                    firestore.insertToArrayField(trackingLoc, "boards", mapOf(
                        "what" to "all",
                        "ref" to boardDoc.path
                    ))
                }
                onSuccess()
            }
        }else{
            onFailed()
        }
    }

    suspend fun isJoinedThisBoard(boardId: String): Boolean{
        local.appDao.getBoardWithMembers(boardId)?.let { boardWithMembers->
            UserWrapper.getInstance()?.currentUserEmail?.let { email->
                boardWithMembers.members.forEach { member->
                    if(member.email == email){
                        return true
                    }
                }
            }
        }
        return false
    }

    suspend fun joinBoard(workspaceId: String, boardId: String, onSuccess: () -> Unit){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            val boardRef = firestore.getBoardDoc(workspaceId, boardId)
                if(firestore.insertToArrayField(
                    boardRef,
                    "members",
                    RemoteMemberRef(email, firestore.getMemberDoc(email).path, RemoteMemberRef.ROLE_MEMBER)
                )){
                local.appDao.getBoardWithMembers(boardId)?.let { boardWithMember->
                    val activityId = "activity_${System.currentTimeMillis()}"
                    val activityDoc = firestore.getActivityDoc(boardRef, activityId)
                    UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                        val message = MessageMaker.getJoinBoardMessage(boardId, boardWithMember.board.boardName)
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
                    boardWithMember.members.let { listMember->
                        listMember.forEach { mem->
                            val tracking = firestore.getTrackingDoc(mem.email)
                            firestore.insertToArrayField(
                                tracking,
                                "boards",
                                mapOf(
                                    "what" to "all",
                                    "ref" to boardRef.path
                                )
                            )
                            firestore.insertToArrayField(
                                tracking,
                                "activities",
                                activityDoc.path
                            )
                        }
                    }
                }
                firestore.insertToArrayField(
                    firestore.getTrackingDoc(email),
                    "boards",
                    mapOf(
                        "what" to "all",
                        "ref" to boardRef.path
                    )
                )
                firestore.insertToArrayField(
                    firestore.getTrackingDoc(email),
                    "activities",
                    boardRef.path
                )
                onSuccess()
            }
        }
    }

    suspend fun isOwnerOfThisBoard(boardId: String): Boolean{
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            local.memberBoardDao.getRelByEmailAndBoardId(email, boardId)?.let {
                if(it.role == MemberBoardRel.ROLE_OWNER){
                    return true
                }
            }
        }
        return false
    }

    suspend fun inviteMember(workspace: Workspace, otherEmail: String): String{
        UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
            val email = member.email
            if (email == otherEmail) {
                return "You can invite yourself"
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(otherEmail).matches()) {
                return "Invalid email address"
            }
            firestore.simpleGetDocumentModel(RemoteMember::class.java, firestore.getMemberDoc(otherEmail))?.toMember()?.let { m ->
                val id = "invitation_${workspace.workspaceId}_"
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
                remoteActivity.message = MessageMaker.getWorkspaceInvitationSenderMessage(
                    workspace.workspaceId,
                    workspace.workspaceName,
                    m.email,
                    m.name
                )
                sendInviteNotification(email, remoteActivity)
                remoteActivity.activityId = id + System.currentTimeMillis()
                remoteActivity.actor = otherEmail
                remoteActivity.activityType = Activity.TYPE_INVITATION_WORKSPACE
                remoteActivity.message = MessageMaker.getWorkspaceInvitationReceiverMessage(
                    workspace.workspaceId,
                    workspace.workspaceName,
                    member.email,
                    member.name
                )
                sendInviteNotification(otherEmail, remoteActivity)
            } ?: return "No such member exist"
        } ?: return "Something went wrong"
        return "Sent invitation successfully"
    }

    suspend fun getBoardByEmail(email: String): List<MemberBoardRel>{
        return local.memberBoardDao.getRelsByEmailId(email)
    }

    fun getMemberDoc(email: String): DocumentReference{
        return firestore.getMemberDoc(email)
    }

    suspend fun notifyInfoChanged(email: String) {
        val listMembers: MutableSet<Member> = mutableSetOf()
        local.appDao.getMemberWithWorkspacesNoFlow(email)?.workspaces?.forEach { workspace->
            local.appDao.getMemberByWorkspaceId(workspace.workspaceId).forEach {
                listMembers.add(it)
            }
        }

        if(listMembers.isEmpty()){
            firestore.insertToArrayField(firestore.getTrackingDoc(email), "info", email)
        }else{
            listMembers.forEach {
                firestore.insertToArrayField(firestore.getTrackingDoc(it.email), "info", email)
            }
        }
    }

    suspend fun uploadAvatar(email: String, avatarUri: String, contentResolver: ContentResolver): String{
        if(avatarUri.isNotEmpty()){
            try{
                storage.uploadImage(
                    storage.getAvatarLocation(email),
                    contentResolver,
                    avatarUri.toUri()
                )?.let {
                    return it.toString()
                }
            }catch (e: Exception){
                trace(e)
            }
        }
        return ""
    }

    suspend fun changePassword(email: String, oldPassword: String, newPassword: String) = suspendCancellableCoroutine<String> { cont->
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        firebaseUser?.let { user ->
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    firebaseUser.updatePassword(newPassword)
                        .addOnSuccessListener {
                            cont.safeResume { "" }
                        }
                        .addOnFailureListener { error ->
                            trace(error)
                            cont.safeResume { "Failed to change password" }
                        }
                }
                .addOnFailureListener { e ->
                    trace(e)
                    cont.safeResume { "Your old password is incorrect" }
                }
        }
    }

    suspend fun saveMemberInfoToFirestore(email: String, name: String, avatar: String, bio: String): Boolean {
        val remoteMember =  RemoteMember(email, name, avatar, bio).info()
        val doc = getMemberDoc(email)
        if (firestore.mergeDocument(doc, remoteMember)) {
            // succeeded
            notifyInfoChanged(email)
            return true
        } else {
            return false
        }
    }

    /**
     * this is not an atomic transaction
     *
     */
    suspend fun addWorkspace(
        name: String,
        type: String,
        desc: String,
        onFailed: () -> Unit,
        onSuccess: () -> Unit
    ){
        UserWrapper.getInstance()?.getCurrentUser()?.let {
            val workspaceId = "${it.email}_${name}"
            val memLoc = firestore.getMemberDoc(it.email)
            val loc = firestore.getWorkspaceDoc(it.email, name)
            val data = RemoteWorkspace(
                workspaceId,
                name,
                type,
                desc,
                listOf(
                    RemoteMemberRef(it.email, memLoc.path, RemoteMemberRef.ROLE_LEADER)
                )
            )
            if(firestore.addDocument(loc,data)){
                if(firestore.insertToArrayField(memLoc, "workspaces", RemoteWorkspaceRef(workspaceId, loc.path))){
                    data.toModel()?.let { ws->
                        local.workspaceDao.insertOne(ws)
                        MemberWorkspaceRel(it.email, workspaceId).let { rel->
                            local.memberWorkspaceDao.insertOne(rel)
                        }
                    }
                    onSuccess()
                }else{
                    // failed
                    onFailed()
                }
            }else{
                onFailed()
            }
        }
    }

    suspend fun createNewList(
        boardDoc: DocumentReference,
        boardWithLists: BoardWithLists,
        workspaceId: String,
        boardId: String,
        listName: String,
        onFailed: () -> Unit,
        onSuccess: () -> Unit
    ){
        val listId = "${boardId}_${listName}_${System.currentTimeMillis()}"
        val listDoc = firestore.getListDoc(workspaceId, boardId, listId)
        boardWithLists.lists.size.let { position->
            val remoteList = RemoteList(
                listId,
                listName,
                listDoc.path,
                boardId,
                position
            )
            if(firestore.mergeDocument(listDoc, remoteList)){
                // notify other members
                val activityId = "activity_${System.currentTimeMillis()}"
                val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                    val message = MessageMaker.getCreateListMessage(boardId,
                        boardWithLists.board.boardName, listName)
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
                local.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                    listMember.forEach { mem->
                        val tracking = firestore.getTrackingDoc(mem.email)
                        firestore.insertToArrayField(tracking, "lists", listDoc.path)
                        firestore.insertToArrayField(tracking, "activities", activityDoc.path)
                    }
                }
                onSuccess()
            }else{
                //failed
                onFailed()
            }
        }
    }

    suspend fun createNewCard(
        boardWithLists: BoardWithLists,
        boardDoc: DocumentReference,
        workspaceId: String,
        boardId: String,
        listId: String,
        cardName: String,
        onFailed: () -> Unit
    ){
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
            if(firestore.mergeDocument(cardLoc, remoteCard)){
                // notify other members
                val activityId = "activity_${System.currentTimeMillis()}"
                val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                    val message = MessageMaker.getCreateCardMessage(boardId,
                        boardWithLists.board.boardName, cardId, cardName)
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

                local.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
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
                onFailed()
            }
        }
    }

    suspend fun fetchAllActivity(workspaceId: String, boardId: String) {
        val col = firestore.getActivityCol(workspaceId, boardId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteActivity::class.java)?.let { remoteActivity ->
                remoteActivity.toModel()?.let {
                    local.activityDao.insertOne(it)
                }
            }
        }
    }

    suspend fun fetchAllLabels(workspaceId: String, boardId: String): Boolean{
        val col = firestore.getLabelCol(workspaceId, boardId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteLabel::class.java)?.let { remoteLabel ->
                remoteLabel.toLabel()?.let {
                    local.labelDao.insertOne(it)
                }
            }
        } ?: return false
        return true
    }

    suspend fun fetchAllList(workspaceId: String, boardId: String){
        val col = firestore.getListCol(workspaceId, boardId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteList::class.java)?.let { remoteList ->
                remoteList.toModel()?.let {
                    local.listDao.insertOne(it)
                }
            }
            fetchAllCardInList(workspaceId, boardId, document.id)
        }
    }

    suspend fun fetchAllCardInList(workspaceId: String, boardId: String, listId: String){
        val col = firestore.getCardCol(workspaceId, boardId, listId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteCard::class.java)?.let { remoteCard ->
                remoteCard.toModel()?.let {
                    local.cardDao.insertOne(it)
                    addCardLabelRel(it.cardId, remoteCard.labels)
                    addCardMemberRel(it.cardId, remoteCard.members)
                    fetchAttachmentsInCard(workspaceId, boardId, listId, it.cardId)
                }
            }
        }
    }

    suspend fun fetchAttachmentsInCard(workspaceId: String, boardId: String, listId: String, cardId: String){
        val col = firestore.getAttachmentCol(workspaceId, boardId, listId, cardId)
        firestore.getCol(col)?.documents?.forEach { document->
            document.toObject(RemoteAttachment::class.java)?.let { remoteAttachment ->
                remoteAttachment.toModel().let {
                    local.attachmentDao.insertOne(it)
                }
            }
        }
    }

    suspend fun addCardLabelRel(cardId: String, remoteCard: List<RemoteLabelRef>){
        local.cardLabelDao.getRelByCardId(cardId).forEach {
            local.cardLabelDao.deleteOne(it)
        }
        remoteCard.forEach { remoteLabelRef ->
            remoteLabelRef.labelId?.let {
                local.cardLabelDao.insertOne(CardLabelRel(cardId, it))
            }
        }
    }

    suspend fun addCardMemberRel(cardId: String, remoteCard: List<RemoteMemberRef>){
        local.memberCarDao.getRelByCardId(cardId).forEach {
            local.memberCarDao.deleteOne(it)
        }
        remoteCard.forEach { remoteMemberRef ->
            remoteMemberRef.email?.let { e->
                local.memberCarDao.insertOne(
                    MemberCardRel(
                        e,
                        cardId,
                        remoteMemberRef.role
                    )
                )
            }
        }
    }

    suspend fun leaveBoard(
        boardWithLists: BoardWithLists,
        boardDoc: DocumentReference,
        workspaceId: String,
        boardId: String,
        prefManager: PrefManager,
        onSuccess: ()-> Unit
    ){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            val memberDoc = firestore.getMemberDoc(email)
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
                val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                    val message = MessageMaker.getLeaveBoardMessage(boardId,
                        boardWithLists.board.boardName
                    )
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

                local.memberBoardDao.getRelByEmailAndBoardId(email, boardId)?.let {
                    local.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
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

    suspend fun closeBoard(workspaceId: String, boardId: String, prefManager: PrefManager){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
            if(firestore.mergeDocument(
                    boardDoc,
                    mapOf("boardStatus" to Board.STATUS_CLOSED)
                )){
                prefManager.putString("$email+$boardId", "")
                // notify all members in workspace
                local.appDao.getWorkspaceWithMembersNoFlow(workspaceId)?.members?.let { listMember->
                    listMember.forEach { mem->
                        val tracking = firestore.getTrackingDoc(mem.email)
                        firestore.insertToArrayField(
                            tracking,
                            "closeBoards",
                            boardDoc.path
                        )
                    }
                }
            }
        }
    }

    suspend fun archiveList(
        boardWithLists: BoardWithLists,
        listId: String,
        email: String,
        boardCardHelper: BoardCardHelper,
        onSuccess: () -> Unit,
    ){
        boardWithLists.board.let { b->
            val cards = local.cardDao.getActiveCardsByListId(listId)
            cards.forEach {
                boardCardHelper.archiveCard(
                    b,
                    it,
                    email
                ){}
            }
            if(cards.isNotEmpty()){
                onSuccess()
            }
        }
    }

    suspend fun changeVisibility(
        boardWithLists: BoardWithLists,
        boardDoc: DocumentReference,
        boardId: String,
        newVisibility: String,
        onSuccess: () -> Unit
    ){
        boardDoc.let { doc->
            if(boardWithLists.board.boardVisibility != newVisibility){
                if(firestore.mergeDocument(
                        doc,
                        mapOf("boardVisibility" to Board.STATUS_CLOSED)
                    )){
                    local.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
                            val message = MessageMaker.getChangeBoardVisMessage(boardId,
                                boardWithLists.board.boardName, newVisibility)
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
                        onSuccess()
                    }
                }
            }
        }
    }

    suspend fun inviteMemberToBoard(
        boardWithLists: BoardWithLists,
        otherEmail: String,
        workspaceId: String
    ): String{
        UserWrapper.getInstance()?.getCurrentUser()?.let { member ->
            val email = member.email
            if (email == otherEmail) {
                return ("You can't invite yourself")
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(otherEmail).matches()) {
                return ("Invalid email address")
            }
            boardWithLists.board.let { b->
                firestore.simpleGetDocumentModel(RemoteMember::class.java, firestore.getMemberDoc(otherEmail))?.toMember()?.let { m ->
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
                    sendInviteNotification(email, remoteActivity)
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
                    sendInviteNotification(otherEmail, remoteActivity)
                    return ("Sent invitation successfully")
                } ?: return ("No such member exist")
            }
        }
        return "Something went wrong"
    }

    suspend fun deleteList(
        viewModel: BaseViewModel,
        email: String,
        boardWithLists: BoardWithLists,
        boardCardHelper: BoardCardHelper,
        list: com.tnh.mollert.datasource.local.model.List,
        onSuccess: () -> Unit
    ){
        boardWithLists.board.let { b->
            val cards = local.cardDao.getActiveCardsByListId(list.listId)
            cards.forEach {
                boardCardHelper.deleteThisCard(
                    b,
                    it,
                    email,
                    viewModel,
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

                if (local.listDao.deleteOne(list) > 0) {
                    local.appDao.getBoardWithMembers(b.boardId)?.members?.let { listMember ->
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
            onSuccess()
        }
    }

    suspend fun changeBoardName(
        name: String,
        workspaceId: String,
        boardId: String,
        email: String,
        onSuccess: () -> Unit
    ) {
        val boardDoc = firestore.getBoardDoc(workspaceId, boardId)
            val data = mapOf(
                "boardName" to name
            )
        if(firestore.mergeDocument(boardDoc, data)){
            val boardWithMember = local.appDao.getBoardWithMembers(boardId)
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
            onSuccess()
        }
    }

    suspend fun changeListName(
        name: String,
        workspaceId: String,
        boardId: String,
        list: com.tnh.mollert.datasource.local.model.List,
        email: String,
        onSuccess: () -> Unit
    ) {
        val listDoc = firestore.getListDoc(workspaceId, boardId, list.listId)
        val data = mapOf(
            "name" to name
        )
        if(firestore.mergeDocument(listDoc, data)){
            val boardWithMember = local.appDao.getBoardWithMembers(boardId)
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
            onSuccess()
        }
    }

    suspend fun checkAndFetchList(prefManager: PrefManager, workspaceId: String, boardId: String){
        UserWrapper.getInstance()?.currentUserEmail?.let { email->
            if(prefManager.getString("$email+$boardId").isEmpty()){
                "Fetching all board content".logAny()
                fetchAllLabels(workspaceId, boardId)
                fetchAllList(workspaceId, boardId)
                fetchAllActivity(workspaceId, boardId)
                prefManager.putString("$email+$boardId", "synced")
            }
        }
    }

    fun getBoardDoc(workspaceId: String, boardId: String) = firestore.getBoardDoc(workspaceId, boardId)

    suspend fun getBoardOwner(boardId: String): Member?{
        return local.memberDao.getBoardOwner(boardId)
    }

    suspend fun getWorkspaceWithBoardsNoFlow(workspaceId: String) = local.appDao.getWorkspaceWithBoardsNoFlow(workspaceId)

    companion object{
        @Volatile
        private lateinit var instance: AppRepository

        fun getInstance(
            local: DataSource,
            firestore: FirestoreAction,
            storage: StorageFunction
        ): AppRepository{
            if(::instance.isInitialized.not()){
                instance = AppRepository(local, firestore, storage)
            }
            return instance
        }
    }
}