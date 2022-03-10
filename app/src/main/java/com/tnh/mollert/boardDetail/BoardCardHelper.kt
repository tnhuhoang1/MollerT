package com.tnh.mollert.boardDetail

import androidx.lifecycle.viewModelScope
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.remote.model.RemoteActivity
import com.tnh.mollert.utils.FirestoreAction
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import kotlinx.coroutines.launch

class BoardCardHelper(
    private val repository: DataSource,
    private val firestore: FirestoreAction
) {


    suspend fun achieveCard(
        board: Board,
        card: Card,
        email: String,
        onSuccess: () -> Unit
    ){
        val cardDoc = firestore.getCardDoc(board.workspaceId, board.boardId, card.listIdPar, card.cardId)
        val boardDoc = firestore.getBoardDoc(board.workspaceId, board.boardId)
        cardDoc.let { doc->
            if(firestore.mergeDocument(
                    doc,
                    mapOf(
                        "status" to Card.STATUS_ACHIEVED,
                    )
                )){
                val activityId = "activity_${System.currentTimeMillis()}"
                val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                val message = MessageMaker.getCardAchievedMessage(card.cardId, card.cardName, board.boardId, board.boardName)
                val remoteActivity = RemoteActivity(
                    activityId,
                    email,
                    board.boardId,
                    card.cardId,
                    message,
                    false,
                    Activity.TYPE_ACTION,
                    System.currentTimeMillis()
                )
                firestore.addDocument(activityDoc, remoteActivity)

                repository.appDao.getBoardWithMembers(board.boardId)?.members?.let { listMember->
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
                    onSuccess()
                }
            }
        }
    }


    fun deleteThisCard(
        board: Board,
        card: Card,
        email: String,
        baseViewModel: BaseViewModel,
        doOnSuccess: () -> Unit
    ){
        val cardDoc = firestore.getCardDoc(board.workspaceId, board.boardId, card.listIdPar, card.cardId)
        val boardDoc = firestore.getBoardDoc(board.workspaceId, board.boardId)
        cardDoc.let { doc->
            card.let { c->
                baseViewModel.viewModelScope.launch {
                    if (firestore.deleteDocument(
                            doc,
                        )
                    ) {
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getDelCardMessage(
                            card.cardId,
                            card.cardName,
                            board.boardId,
                            board.boardName
                        )
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            board.boardId,
                            card.cardId,
                            message,
                            false,
                            Activity.TYPE_INFO,
                            System.currentTimeMillis()
                        )
                        firestore.addDocument(activityDoc, remoteActivity)

                        repository.activityDao.getActivityByCardId(c.cardId).forEach {
                            deleteActivity(board, card, email, it, baseViewModel){}
                        }
                        repository.attachmentDao.getAllByCardIdNoFlow(c.cardId).forEach {
                            deleteAttachment(board, card, email, it, baseViewModel){}
                        }
                        repository.workDao.getWorksByCardId(c.cardId).forEach {
                            deleteWork(board, card, email, it, baseViewModel){}
                        }
                        if (repository.cardDao.deleteOne(c) > 0) {
                            repository.appDao.getBoardWithMembers(board.boardId)?.members?.let { listMember ->
                                listMember.forEach { mem ->
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
                }
            }
        }
    }

    fun deleteActivity(
        board: Board,
        card: Card,
        email: String,
        activity: Activity,
        baseViewModel: BaseViewModel,
        onSuccess: () -> Unit
    ) {
        val boardDoc = firestore.getBoardDoc(board.workspaceId, board.boardId)
        val activityDoc = firestore.getActivityDoc(board.workspaceId, board.boardId, activity.activityId)
        baseViewModel.viewModelScope.launch {
            if(firestore.deleteDocument(
                    activityDoc,
                )){
                if(repository.activityDao.deleteOne(activity) > 0){
                    repository.appDao.getBoardWithMembers(board.boardId)?.members?.let { listMember->
                        val activityId = "activity_${System.currentTimeMillis()}"
                        val activityDoc1 = firestore.getActivityDoc(boardDoc, activityId)
                        val message = MessageMaker.getDelCommendMessage(card.cardId, card.cardName, board.boardId, board.boardName)
                        val remoteActivity = RemoteActivity(
                            activityId,
                            email,
                            board.boardId,
                            card.cardId,
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
                        onSuccess()
                    }
                }
            }
        }
    }

    fun deleteAttachment(
        board: Board,
        card: Card,
        email: String,
        attachment: Attachment,
        baseViewModel: BaseViewModel,
        onSuccess: () -> Unit
    ) {
        val cardDoc = firestore.getCardDoc(board.workspaceId, board.boardId, card.listIdPar, card.cardId)
        val boardDoc = firestore.getBoardDoc(board.workspaceId, board.boardId)
        cardDoc.let { doc->
            val attachmentDoc = firestore.getAttachmentDoc(doc, attachment.attachmentId)
            baseViewModel.viewModelScope.launch {
                if(firestore.deleteDocument(
                        attachmentDoc,
                    )){
                    if(repository.attachmentDao.deleteOne(attachment) > 0){
                        repository.appDao.getBoardWithMembers(board.boardId)?.members?.let { listMember->
                            val activityId = "activity_${System.currentTimeMillis()}"
                            val activityDoc1 = firestore.getActivityDoc(boardDoc, activityId)
                            val message = MessageMaker.getDelAttachmentMessage(card.cardId, card.cardName, board.boardId, board.boardName)
                            val remoteActivity = RemoteActivity(
                                activityId,
                                email,
                                board.boardId,
                                card.cardId,
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
                            onSuccess()
                        }
                    }
                }
            }
        }
    }

    fun deleteWork(
        board: Board,
        card: Card,
        email: String,
        work: Work,
        baseViewModel: BaseViewModel,
        onSuccess: () -> Unit
    ) {
        val cardDoc = firestore.getCardDoc(board.workspaceId, board.boardId, card.listIdPar, card.cardId)
        val boardDoc = firestore.getBoardDoc(board.workspaceId, board.boardId)
        cardDoc.let { doc->
            val workDoc = firestore.getWorkDoc(doc, work.workId)
            baseViewModel.viewModelScope.launch {
                if(firestore.deleteDocument(
                        workDoc,
                    )){
                    if(repository.workDao.deleteOne(work) > 0){
                        repository.appDao.getBoardWithMembers(board.boardId)?.members?.let { listMember->
                            val activityId = "activity_${System.currentTimeMillis()}"
                            val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                            val message = MessageMaker.getDelWorkMessage(work.workName, card.cardId, card.cardName, board.boardId, board.boardName)
                            val remoteActivity = RemoteActivity(
                                activityId,
                                email,
                                board.boardId,
                                card.cardId,
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
                                        deleteTask(board, card, email, task, baseViewModel){}
                                    }
                                }
                                firestore.insertToArrayField(
                                    firestore.getTrackingDoc(mem.email),
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
    }

    fun deleteTask(
        board: Board,
        card: Card,
        email: String,
        task: Task,
        baseViewModel: BaseViewModel,
        onSuccess: () -> Unit
    ){
        val cardDoc = firestore.getCardDoc(board.workspaceId, board.boardId, card.listIdPar, card.cardId)
        val boardDoc = firestore.getBoardDoc(board.workspaceId, board.boardId)
        cardDoc.let { doc->
            val taskDoc = firestore.getTaskDoc(doc, task.workId, task.taskId)
            baseViewModel.viewModelScope.launch {
                if(firestore.deleteDocument(
                        taskDoc,
                    )){
                    if(repository.taskDao.deleteOne(task) > 0){
                        repository.appDao.getBoardWithMembers(board.boardId)?.members?.let { listMember->
                            val activityId = "activity_${System.currentTimeMillis()}"
                            val activityDoc = firestore.getActivityDoc(boardDoc, activityId)
                            val message = MessageMaker.getDelTaskMessage(task.taskName, card.cardId, card.cardName, board.boardId, board.boardName)
                            val remoteActivity = RemoteActivity(
                                activityId,
                                email,
                                board.boardId,
                                card.cardId,
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
                            onSuccess()
                        }
                    }
                }
            }
        }
    }

}