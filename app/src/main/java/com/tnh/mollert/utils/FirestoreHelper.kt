package com.tnh.mollert.utils

import com.google.firebase.firestore.*
import com.tnh.mollert.datasource.remote.model.RemoteModel
import com.tnh.tnhlibrary.trace
import kotlinx.coroutines.suspendCancellableCoroutine

class FirestoreHelper private constructor() : FirestoreAction {
    private val store = FirebaseFirestore.getInstance()


    /**
     * note: It will override the document
     *
     * if you need to update entire document, use [mergeDocument]
     */
    override fun addDocument(
        document: DocumentReference,
        data: RemoteModel,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit,
    ){
        document.set(data)
            .addOnSuccessListener{
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override suspend fun addDocument(
        document: DocumentReference,
        data: RemoteModel,
    ) = suspendCancellableCoroutine<Boolean> { cont->
        addDocument(
            document,
            data,
            {
                trace(it)
                cont.safeResume { false }
            },
            {
                cont.safeResume { true }
            }
        )
    }

    /**
     * update single field of the document
     */
    override fun updateDocument(
        document: DocumentReference,
        field: String,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ){
        document.update(
            field, data
        ).addOnFailureListener(onFailure)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    override suspend fun updateDocument(
        document: DocumentReference,
        field: String,
        data: Any
    ) = suspendCancellableCoroutine<Boolean> { cont->
        updateDocument(
            document,
            field,
            data,
            {
                trace(it)
                cont.safeResume { false }
            }
        ){
            cont.safeResume { true }
        }
    }

    override fun updateDocument(
        document: DocumentReference,
        map: Map<String, Any>,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ){
        document.update(map)
            .addOnFailureListener(onFailure)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    override suspend fun updateDocument(
        document: DocumentReference,
        map: Map<String, Any>
    ) = suspendCancellableCoroutine<Boolean> { cont->
        updateDocument(
            document,
            map,
            {
                trace(it)
                cont.safeResume { false }
            }
        ){
            cont.safeResume { true }
        }
    }

    override fun deleteDocument(
        document: DocumentReference,
        onFailure: (Exception) -> Unit,
        onSuccess: () -> Unit
    ){
        document.delete()
            .addOnFailureListener(onFailure)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    override suspend fun deleteDocument(document: DocumentReference) = suspendCancellableCoroutine<Boolean> { cont->
        deleteDocument(
            document,
            {
                trace(it)
                cont.safeResume { false }
            }
        ){
            cont.safeResume { true }
        }
    }

    override fun insertToArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ){
        document.update(field, FieldValue.arrayUnion(data))
            .addOnFailureListener(onFailure)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    override suspend fun insertToArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
    ) = suspendCancellableCoroutine<Boolean>{ cont->
        insertToArrayField(
            document,
            field,
            data,
            {
                trace(it)
                cont.safeResume { false }
            }
        ){
            cont.safeResume { true }
        }
    }

    override fun removeFromArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit
    ){
        document.update(field, FieldValue.arrayRemove(data))
            .addOnFailureListener(onFailure)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    override suspend fun removeFromArrayField(
        document: DocumentReference,
        field: String,
        data: Any,
    ) = suspendCancellableCoroutine<Boolean>{ cont->
        removeFromArrayField(
            document,
            field,
            data,
            {
                trace(it)
                cont.safeResume { false }
            }
        ){
            cont.safeResume { true }
        }
    }

    override fun getCol(
        collection: CollectionReference,
        onFailure: (Exception?) -> Unit,
        onSuccess: (QuerySnapshot) -> Unit
    ){
        collection.get()
            .addOnFailureListener {
                onFailure(it)
            }
            .addOnSuccessListener { query->
                onSuccess(query)
            }
    }

    override suspend fun getCol(collection: CollectionReference) = suspendCancellableCoroutine<QuerySnapshot?>{ cont ->
        getCol(
            collection,
            {
                trace(it)
                cont.safeResume { null }
            }
        ){
            cont.safeResume { it }
        }
    }

    override fun mergeDocument(
        document: DocumentReference,
        data: Any,
        onFailure: (Exception?) -> Unit,
        onSuccess: () -> Unit,
    ){
        document.set(data, SetOptions.merge())
            .addOnSuccessListener{
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override suspend fun mergeDocument(
        document: DocumentReference,
        data: Any,
    ) = suspendCancellableCoroutine<Boolean> { cont->
        mergeDocument(
            document,
            data,
            {
                trace(it)
                cont.safeResume { false }
            }
        ){
            cont.safeResume { true }
        }
    }

    override fun getDocRef(refPath: String): DocumentReference{
        return store.document(refPath)
    }

    override fun getColRef(refPath: String): CollectionReference{
        return store.collection(refPath)
    }

    override fun getListCol(workspaceId: String, boardId: String): CollectionReference{
        return getBoardDoc(workspaceId, boardId).collection("lists")
    }

    override fun getCardCol(workspaceId: String, boardId: String, listId: String): CollectionReference{
        return getListDoc(workspaceId, boardId, listId).collection("cards")
    }

    override fun getLabelCol(workspaceId: String, boardId: String): CollectionReference{
        return getBoardDoc(workspaceId, boardId).collection("labels")
    }

    override fun getAttachmentCol(workspaceId: String, boardId: String, listId: String, cardId: String): CollectionReference{
        return getCardDoc(workspaceId, boardId, listId, cardId).collection("attachments")
    }

    override fun getWorkDoc(workspaceId: String, boardId: String, listId: String, cardId: String, workId: String): DocumentReference{
        return getCardDoc(workspaceId, boardId, listId, cardId).collection("works").document(workId)
    }

    override fun getWorkDoc(cardDoc: DocumentReference, workId: String): DocumentReference{
        return cardDoc.collection("works").document(workId)
    }

    override fun getTaskDoc(cardDoc: DocumentReference, workId: String, taskId: String): DocumentReference{
        return cardDoc.collection("works").document(workId).collection("tasks").document(taskId)
    }

    override fun getTaskCol(cardDoc: DocumentReference, workId: String): CollectionReference{
        return cardDoc.collection("works").document(workId).collection("tasks")
    }

    override fun getWorkDoc(workspaceId: String, boardId: String, listId: String, cardId: String): CollectionReference{
        return getCardDoc(workspaceId, boardId, listId, cardId).collection("works")
    }

    override fun getMemberDoc(email: String): DocumentReference{
        return getDocRef("$MEMBER_ROOT_COL/$email")
    }

    override fun getTrackingDoc(email: String): DocumentReference{
        return getDocRef("$TRACKING_ROOT_COL/$email")
    }

    override fun getActivityDoc(workspaceId: String, boardId: String, activityId: String): DocumentReference{
        return getBoardDoc(workspaceId, boardId).collection("activities").document(activityId)
    }

    override fun getActivityDoc(boardDoc: DocumentReference, activityId: String): DocumentReference{
        return boardDoc.collection("activities").document(activityId)
    }

    override fun getActivityCol(workspaceId: String, boardId: String): CollectionReference{
        return getBoardDoc(workspaceId, boardId).collection("activities")
    }

    override fun getListDoc(workspaceId: String, boardId: String, listId: String): DocumentReference{
        return getBoardDoc(workspaceId, boardId).collection("lists").document(listId)
    }

    override fun getCardDoc(workspaceId: String, boardId: String, listId: String, cardId: String): DocumentReference{
        return getListDoc(workspaceId, boardId, listId).collection("cards").document(cardId)
    }

    override fun getAttachmentDoc(workspaceId: String, boardId: String, listId: String, cardId: String, attachmentId: String): DocumentReference{
        return getCardDoc(workspaceId, boardId, listId, cardId).collection("attachments").document(attachmentId)
    }

    override fun getAttachmentDoc(cardDoc: DocumentReference, attachmentId: String): DocumentReference{
        return cardDoc.collection("attachments").document(attachmentId)
    }

    override fun getBoardDoc(workspaceId: String, boardId: String): DocumentReference{
        return getDocRef("$WORKSPACE_ROOT_COL/${workspaceId}/boards/${boardId}")
    }

    override fun getLabelDoc(workspaceId: String, boardId: String, labelId: String): DocumentReference{
        return getBoardDoc(workspaceId, boardId).collection("labels").document(labelId)
    }

    override fun getBoardCol(workspaceId: String): CollectionReference{
        return getColRef("$WORKSPACE_ROOT_COL/$workspaceId/boards")
    }

    override fun getWorkspaceDoc(email: String, workspaceName: String): DocumentReference{
        return getDocRef("$WORKSPACE_ROOT_COL/${email}_${workspaceName}")
    }

    override fun getWorkspaceDoc(workspaceId: String): DocumentReference{
        return getDocRef("$WORKSPACE_ROOT_COL/$workspaceId")
    }

    override fun getDocument(
        document: DocumentReference,
        onFailure: (Exception)-> Unit,
        onSuccess: (data: DocumentSnapshot) -> Unit
    ){
        document.get()
            .addOnFailureListener(onFailure)
            .addOnSuccessListener(onSuccess)
    }

    override fun <T: RemoteModel>getDocumentModel(
        type: Class<T>,
        document: DocumentReference,
        onFailure: (Exception?) -> Unit,
        onSuccess: (T?) -> Unit
    ){
        getDocument(
            document,
            onFailure
        ) {
            try {
                onSuccess(it.toObject(type))
            } catch (e: Exception) {
                trace(e)
                onFailure(e)
            }
        }
    }

    override suspend fun <T: RemoteModel>simpleGetDocumentModel(
        type: Class<T>,
        document: DocumentReference
    ) = suspendCancellableCoroutine<T?> { cont->
        getDocumentModel(
            type,
            document,
            {
                trace(it)
                cont.safeResume { null }
            },
            {
                cont.safeResume { it }
            }
        )
    }

    override fun listenDocument(
        document: DocumentReference,
        onFailure: (Exception?) -> Unit,
        onSuccess: (DocumentSnapshot?) -> Unit
    ): ListenerRegistration{
        return document.addSnapshotListener { value, error ->
            if(error != null){
                onFailure(error)
                return@addSnapshotListener
            }
            onSuccess(value)
        }
    }


    companion object{
        @Volatile
        private lateinit var instance: FirestoreHelper

        fun getInstance(): FirestoreHelper{
            if(::instance.isInitialized.not()){
                instance = FirestoreHelper()
            }
            return instance
        }
        const val MEMBER_ROOT_COL = "members"
        const val WORKSPACE_ROOT_COL = "workspaces"
        const val TRACKING_ROOT_COL = "tracking"
    }
}