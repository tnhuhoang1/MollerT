package com.tnh.mollert.utils

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tnh.mollert.datasource.remote.model.RemoteModel
import com.tnh.tnhlibrary.trace
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirestoreHelper {
    private val store = FirebaseFirestore.getInstance()


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


    fun mergeDocument(
        document: DocumentReference,
        data: RemoteModel,
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

    fun getDocRef(refPath: String): DocumentReference{
        return store.document(refPath)
    }

    fun getMemberDoc(email: String): DocumentReference{
        return getDocRef("$MEMBER_ROOT_COL/$email")
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
    }
}