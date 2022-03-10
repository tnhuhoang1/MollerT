package com.tnh.mollert.utils

import com.google.firebase.firestore.*
import com.tnh.mollert.datasource.remote.model.RemoteModel

interface FirestoreAction {
    /**
     * note: It will override the document
     *
     * if you need to update entire document, use [mergeDocument]
     */
    fun addDocument(
        document: DocumentReference,
        data: RemoteModel,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit,
    )

    suspend fun addDocument(
        document: DocumentReference,
        data: RemoteModel,
    ): Boolean

    /**
     * update single field of the document
     */
    fun updateDocument(
        document: DocumentReference,
        field: String,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    )

    suspend fun updateDocument(
        document: DocumentReference,
        field: String,
        data: Any
    ): Boolean

    fun updateDocument(
        document: DocumentReference,
        map: Map<String, Any>,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    )

    suspend fun updateDocument(
        document: DocumentReference,
        map: Map<String, Any>
    ): Boolean

    fun deleteDocument(
        document: DocumentReference,
        onFailure: (Exception) -> Unit,
        onSuccess: () -> Unit
    )

    suspend fun deleteDocument(document: DocumentReference): Boolean
    fun insertToArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    )

    suspend fun insertToArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
    ): Boolean

    fun removeFromArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    )

    suspend fun removeFromArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
    ): Boolean

    fun getCol(
        collection: CollectionReference,
        onFailure: (Exception?) -> Unit,
        onSuccess: (QuerySnapshot) -> Unit
    )

    suspend fun getCol(collection: CollectionReference): QuerySnapshot?
    fun mergeDocument(
        document: DocumentReference,
        data: Any,
        onFailure: (Exception?) -> Unit = {},
        onSuccess: () -> Unit,
    )

    suspend fun mergeDocument(
        document: DocumentReference,
        data: Any,
    ): Boolean

    fun getDocRef(refPath: String): DocumentReference
    fun getColRef(refPath: String): CollectionReference
    fun getListCol(workspaceId: String, boardId: String): CollectionReference
    fun getCardCol(workspaceId: String, boardId: String, listId: String): CollectionReference
    fun getLabelCol(workspaceId: String, boardId: String): CollectionReference
    fun getAttachmentCol(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String
    ): CollectionReference

    fun getWorkDoc(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String,
        workId: String
    ): DocumentReference

    fun getWorkDoc(cardDoc: DocumentReference, workId: String): DocumentReference
    fun getTaskDoc(cardDoc: DocumentReference, workId: String, taskId: String): DocumentReference
    fun getTaskCol(cardDoc: DocumentReference, workId: String): CollectionReference
    fun getWorkDoc(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String
    ): CollectionReference

    fun getMemberDoc(email: String): DocumentReference
    fun getTrackingDoc(email: String): DocumentReference
    fun getActivityDoc(workspaceId: String, boardId: String, activityId: String): DocumentReference
    fun getActivityDoc(boardDoc: DocumentReference, activityId: String): DocumentReference
    fun getActivityCol(workspaceId: String, boardId: String): CollectionReference
    fun getListDoc(workspaceId: String, boardId: String, listId: String): DocumentReference
    fun getCardDoc(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String
    ): DocumentReference

    fun getAttachmentDoc(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String,
        attachmentId: String
    ): DocumentReference

    fun getAttachmentDoc(cardDoc: DocumentReference, attachmentId: String): DocumentReference
    fun getBoardDoc(workspaceId: String, boardId: String): DocumentReference
    fun getLabelDoc(workspaceId: String, boardId: String, labelId: String): DocumentReference
    fun getBoardCol(workspaceId: String): CollectionReference
    fun getWorkspaceDoc(email: String, workspaceName: String): DocumentReference
    fun getWorkspaceDoc(workspaceId: String): DocumentReference
    fun getDocument(
        document: DocumentReference,
        onFailure: (Exception) -> Unit = {},
        onSuccess: (data: DocumentSnapshot) -> Unit = {}
    )

    fun <T : RemoteModel> getDocumentModel(
        type: Class<T>,
        document: DocumentReference,
        onFailure: (Exception?) -> Unit = {},
        onSuccess: (T?) -> Unit = {}
    )

    suspend fun <T : RemoteModel> simpleGetDocumentModel(
        type: Class<T>,
        document: DocumentReference
    ): T?

    fun listenDocument(
        document: DocumentReference,
        onFailure: (Exception?) -> Unit,
        onSuccess: (DocumentSnapshot?) -> Unit
    ): ListenerRegistration
}