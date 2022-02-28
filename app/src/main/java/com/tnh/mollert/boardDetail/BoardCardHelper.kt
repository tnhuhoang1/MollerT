package com.tnh.mollert.boardDetail

import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Activity
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.mollert.datasource.local.model.MessageMaker
import com.tnh.mollert.datasource.remote.model.RemoteActivity
import com.tnh.mollert.utils.FirestoreHelper

class BoardCardHelper(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper
) {


    suspend fun achieveCard(
        board: Board,
        card: Card,
        email: String,
        onSuccess: () -> Unit
    ){
        val cardDoc = firestore.getCardDoc(board.workspaceId, board.boardId, card.listId, card.cardId)
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
}