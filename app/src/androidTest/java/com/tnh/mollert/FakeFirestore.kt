package com.tnh.mollert

import com.google.firebase.firestore.*
import com.tnh.mollert.datasource.remote.model.RemoteModel
import com.tnh.mollert.utils.FirestoreAction
import com.tnh.tnhlibrary.trace
import javax.inject.Inject

class FakeFirestore @Inject constructor(): FirestoreAction {
    private val f = FirebaseFirestore.getInstance()
    override fun addDocument(
        document: DocumentReference,
        data: RemoteModel,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun addDocument(document: DocumentReference, data: RemoteModel): Boolean {
        return true
    }

    override fun updateDocument(
        document: DocumentReference,
        field: String,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateDocument(
        document: DocumentReference,
        field: String,
        data: Any
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun updateDocument(
        document: DocumentReference,
        map: Map<String, Any>,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateDocument(
        document: DocumentReference,
        map: Map<String, Any>
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteDocument(
        document: DocumentReference,
        onFailure: (Exception) -> Unit,
        onSuccess: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDocument(document: DocumentReference): Boolean {
        TODO("Not yet implemented")
    }

    override fun insertToArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun insertToArrayField(
        document: DocumentReference,
        field: String,
        data: Any
    ): Boolean {
        return true
    }

    override fun removeFromArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ) {
    }

    override suspend fun removeFromArrayField(
        document: DocumentReference,
        field: String,
        data: Any
    ): Boolean {
        return true
    }

    override fun getCol(
        collection: CollectionReference,
        onFailure: (Exception?) -> Unit,
        onSuccess: (QuerySnapshot) -> Unit
    ) {

    }

    override suspend fun getCol(collection: CollectionReference): QuerySnapshot? {
        return null
    }

    override fun mergeDocument(
        document: DocumentReference,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ) {
    }

    override suspend fun mergeDocument(document: DocumentReference, data: Any): Boolean {
        return true
    }

    override fun getDocRef(refPath: String): DocumentReference {
        return f.document("test/test")
    }

    override fun getColRef(refPath: String): CollectionReference {
        return f.collection("test")
    }

    override fun getListCol(workspaceId: String, boardId: String): CollectionReference {
        return f.collection("test")
    }

    override fun getCardCol(
        workspaceId: String,
        boardId: String,
        listId: String
    ): CollectionReference {
        return f.collection("test")
    }

    override fun getLabelCol(workspaceId: String, boardId: String): CollectionReference {
        return f.collection("test")
    }

    override fun getAttachmentCol(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String
    ): CollectionReference {
        return f.collection("test")
    }

    override fun getWorkDoc(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String,
        workId: String
    ): DocumentReference {
        return f.document("test/test")
    }

    override fun getWorkDoc(cardDoc: DocumentReference, workId: String): DocumentReference {
        return f.document("test/test")
    }

    override fun getWorkDoc(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String
    ): CollectionReference {
        return f.collection("test")
    }

    override fun getTaskDoc(
        cardDoc: DocumentReference,
        workId: String,
        taskId: String
    ): DocumentReference {
        return f.document("test/test")
    }

    override fun getTaskCol(cardDoc: DocumentReference, workId: String): CollectionReference {
        return f.collection("test")
    }

    override fun getMemberDoc(email: String): DocumentReference {
        return try {
            f.document("test/test")
        }catch (e: Exception){
            trace(e)
            throw Exception("Hello world")
        }
    }

    override fun getTrackingDoc(email: String): DocumentReference {
        return f.document("test/test")
    }

    override fun getActivityDoc(
        workspaceId: String,
        boardId: String,
        activityId: String
    ): DocumentReference {
        return f.document("test/test")
    }

    override fun getActivityDoc(
        boardDoc: DocumentReference,
        activityId: String
    ): DocumentReference {
        return f.document("test/test")
    }

    override fun getActivityCol(workspaceId: String, boardId: String): CollectionReference {
        return f.collection("test")
    }

    override fun getListDoc(
        workspaceId: String,
        boardId: String,
        listId: String
    ): DocumentReference {
        return f.document("test/test")
    }

    override fun getCardDoc(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String
    ): DocumentReference {
        return f.document("test/test")
    }

    override fun getAttachmentDoc(
        workspaceId: String,
        boardId: String,
        listId: String,
        cardId: String,
        attachmentId: String
    ): DocumentReference {
        return f.document("test/test")
    }

    override fun getAttachmentDoc(
        cardDoc: DocumentReference,
        attachmentId: String
    ): DocumentReference {
        return f.document("test/test")
    }

    override fun getBoardDoc(workspaceId: String, boardId: String): DocumentReference {
        return f.document("test/test")
    }

    override fun getLabelDoc(
        workspaceId: String,
        boardId: String,
        labelId: String
    ): DocumentReference {
        return f.document("test/test")
    }

    override fun getBoardCol(workspaceId: String): CollectionReference {
        return f.collection("test")
    }

    override fun getWorkspaceDoc(email: String, workspaceName: String): DocumentReference {
        return f.document("test/test")
    }

    override fun getWorkspaceDoc(workspaceId: String): DocumentReference {
        return f.document("test/test")
    }

    override fun getDocument(
        document: DocumentReference,
        onFailure: (Exception) -> Unit,
        onSuccess: (data: DocumentSnapshot) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun <T : RemoteModel> getDocumentModel(
        type: Class<T>,
        document: DocumentReference,
        onFailure: (Exception?) -> Unit,
        onSuccess: (T?) -> Unit
    ) {

    }

    override suspend fun <T : RemoteModel> simpleGetDocumentModel(
        type: Class<T>,
        document: DocumentReference
    ): T? {
        return null
    }

    override fun listenDocument(
        document: DocumentReference,
        onFailure: (Exception?) -> Unit,
        onSuccess: (DocumentSnapshot?) -> Unit
    ): ListenerRegistration {
        return ListenerRegistration {

        }
    }
}