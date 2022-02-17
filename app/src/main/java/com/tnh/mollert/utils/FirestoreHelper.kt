package com.tnh.mollert.utils

import com.google.firebase.firestore.*
import com.tnh.mollert.datasource.remote.model.RemoteModel
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
                if(cont.isActive){
                    cont.resume(false)
                }
            },
            {
                if(cont.isActive){
                    cont.resume(true)
                }
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
                if(cont.isActive){
                    cont.resume(false)
                }
            }
        ){
            if(cont.isActive){
                cont.resume(true)
            }
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
                if(cont.isActive){
                    cont.resume(false)
                }
            }
        ){
            if(cont.isActive){
                cont.resume(true)
            }
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
                if(cont.isActive){
                    cont.resume(false)
                }
            }
        ){
            if(cont.isActive){
                cont.resume(true)
            }
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
                if(cont.isActive){
                    cont.resume(false)
                }
            }
        ){
            if(cont.isActive){
                cont.resume(true)
            }
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
                if(cont.isActive){
                    cont.resume(false)
                }
            }
        ){
            if(cont.isActive){
                cont.resume(true)
            }
        }
    }

    fun getDocRef(refPath: String): DocumentReference{
        return store.document(refPath)
    }

    fun getMemberDoc(email: String): DocumentReference{
        return getDocRef("$MEMBER_ROOT_COL/$email")
    }

    fun getTrackingDoc(email: String): DocumentReference{
        return getDocRef("$TRACKING_ROOT_COL/$email")
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
        noinline onSuccess: (T) -> Unit = {}
    ){
        getDocument(
            document,
            onFailure
        ) {
            try {
                it.toObject(T::class.java)?.let { obj->
                    onSuccess(obj)
                }
            } catch (e: Exception) {
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
                if(cont.isActive){
                    cont.resume(null)
                }
            },
            {
                if(cont.isActive){
                    cont.resume(it)
                }
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