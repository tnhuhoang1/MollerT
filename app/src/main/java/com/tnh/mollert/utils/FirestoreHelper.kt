package com.tnh.mollert.utils

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tnh.mollert.datasource.remote.model.RemoteModel

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