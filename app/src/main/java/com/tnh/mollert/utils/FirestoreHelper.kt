package com.tnh.mollert.utils

import com.google.firebase.firestore.*
import com.tnh.mollert.datasource.remote.model.RemoteModel
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.trace
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirestoreHelper private constructor(){
    private val store = FirebaseFirestore.getInstance()


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
    ){
        document.set(data)
            .addOnSuccessListener{
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    suspend fun addDocument(
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
    fun updateDocument(
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

    suspend fun updateDocument(
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

    fun updateDocument(
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

    suspend fun updateDocument(
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

    fun insertToArrayField(
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

    suspend fun insertToArrayField(
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

    fun removeFromArrayField(
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

    suspend fun removeFromArrayField(
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

    fun getCol(
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

    suspend fun getCol(collection: CollectionReference) = suspendCancellableCoroutine<QuerySnapshot?>{ cont ->
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

    fun mergeDocument(
        document: DocumentReference,
        data: Any,
        onFailure: (Exception?) -> Unit = {},
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

    suspend fun mergeDocument(
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

    fun getDocRef(refPath: String): DocumentReference{
        return store.document(refPath)
    }

    fun getColRef(refPath: String): CollectionReference{
        return store.collection(refPath)
    }

    fun getMemberDoc(email: String): DocumentReference{
        return getDocRef("$MEMBER_ROOT_COL/$email")
    }

    fun getTrackingDoc(email: String): DocumentReference{
        return getDocRef("$TRACKING_ROOT_COL/$email")
    }

    fun getBoardDoc(workspaceId: String, boardId: String): DocumentReference{
        return getDocRef("$WORKSPACE_ROOT_COL/${workspaceId}/boards/${boardId}")
    }

    fun getBoardCol(workspaceId: String): CollectionReference{
        return getColRef("$WORKSPACE_ROOT_COL/$workspaceId/boards")
    }

    fun getWorkspaceDoc(email: String, workspaceName: String): DocumentReference{
        return getDocRef("$WORKSPACE_ROOT_COL/${email}_${workspaceName}")
    }

    fun getDocument(
        document: DocumentReference,
        onFailure: (Exception)-> Unit = {},
        onSuccess: (data: DocumentSnapshot) -> Unit = {}
    ){
        document.get()
            .addOnFailureListener(onFailure)
            .addOnSuccessListener(onSuccess)
    }

    inline fun <reified T: RemoteModel>getDocumentModel(
        document: DocumentReference,
        noinline onFailure: (Exception?) -> Unit = {},
        noinline onSuccess: (T?) -> Unit = {}
    ){
        getDocument(
            document,
            onFailure
        ) {
            try {
                onSuccess(it.toObject(T::class.java))
            } catch (e: Exception) {
                trace(e)
                onFailure(e)
            }
        }
    }

    suspend inline fun <reified T: RemoteModel>simpleGetDocumentModel(
        document: DocumentReference
    ) = suspendCancellableCoroutine<T?> { cont->
        getDocumentModel<T>(
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

    fun listenDocument(
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