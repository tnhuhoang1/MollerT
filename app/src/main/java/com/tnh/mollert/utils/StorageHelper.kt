package com.tnh.mollert.utils

import android.content.ContentResolver
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tnh.tnhlibrary.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

@Suppress("BlockingMethodInNonBlockingContext")
class StorageHelper private constructor(){
    fun getAttachmentRoot(email: String, workspaceId: String,  boardId: String): StorageReference{
        return storage.getReference("$ATTACHMENT_ROOT/${email}_${workspaceId}_${boardId}")
    }

    fun getUploadAttachmentWithFilename(email: String, workspaceId: String, boardId: String, cardId: String, fileName: String): StorageReference{
        return getAttachmentRoot(email, workspaceId, boardId).child("$cardId/$fileName")
    }

    fun getUploadAttachmentLocation(email: String, workspaceId: String, boardId: String, cardId: String): StorageReference{
        return getAttachmentRoot(email, workspaceId, boardId).child(cardId)
    }

    fun getUploadBackgroundLocation(workspaceId: String, boardId: String): StorageReference{
        return storage.getReference("$BACKGROUND_ROOT/${workspaceId}_${boardId}")
    }

    fun getUploadCoverLocation(cardId: String): StorageReference{
        return storage.getReference("$COVER_ROOT/${cardId}")
    }

    fun getAvatarLocation(email: String): StorageReference{
        return storage.getReference("$AVATAR_ROOT/$email")
    }

    fun getCustomRef(path: String): StorageReference{
        return storage.getReference(path)
    }

    fun getStorage() = storage


    @Throws(IOException::class)
    fun uploadFromStream(
        ref: StorageReference,
        dataStream: InputStream,
        failureBlock: (Exception?) -> Unit,
        successBlock: (url: Uri) -> Unit
    ){
        ref.putStream(dataStream).addOnFailureListener{
            failureBlock(it)
        }.continueWithTask { task->
            if(task.isSuccessful.not()){
                failureBlock(task.exception)
            }
            ref.downloadUrl
        }.addOnSuccessListener {
            dataStream.close()
        }.addOnCompleteListener { task->
            if(task.isSuccessful){
                successBlock(task.result)
            }else{
                failureBlock(task.exception)
            }
        }
    }

    suspend fun uploadAndGetUrlFromStream(
        ref: StorageReference,
        dataStream: InputStream,
        failureBlock: (Exception?) -> Unit = {trace(it)},
        successBlock: (url: Uri) -> Unit = {}
    ) = suspendCancellableCoroutine<Uri?> { continuation->
        uploadFromStream(
            ref,
            dataStream,
            {
                failureBlock(it)
                continuation.safeResume { null }
            },
            {
                if(continuation.isActive){
                    successBlock(it)
                    continuation.safeResume { it }
                }
            }
        )
    }


    /**
     * don't add file extension to image name, the function automatic do it for you
     *
     * @return null if upload failed, url of the image if succeeded
     */
    suspend fun uploadImage(
        parentRef: StorageReference,
        contentResolver: ContentResolver,
        uri: Uri,
        imageName: String = "attachment_${System.currentTimeMillis()}",
    ): Uri?{
        val type = contentResolver.getType(uri)?.let {
            try {
                it.substring(it.lastIndexOf("/") + 1)
            }catch (e: Exception){
                "jpg"
            }
        } ?: "jpg"
        val ref = parentRef.child("$imageName.$type")
        try {
            return withContext(Dispatchers.IO){
                contentResolver.openInputStream(uri)?.let {inputStream ->
                    uploadAndGetUrlFromStream(ref, inputStream)
                }
            }
        }catch (e: Exception){
            trace(e)
        }
        return null
    }

    suspend fun uploadCardCover(
        contentResolver: ContentResolver,
        uri: Uri,
        cardId: String
    ): Uri?{
        return uploadImage(
            getUploadCoverLocation(cardId),
            contentResolver,
            uri,
            cardId
        )
    }

    suspend fun uploadBackgroundImage(
        workspaceId: String,
        boardId: String,
        contentResolver: ContentResolver,
        uri: Uri
    ): Uri?{
        return uploadImage(
            getUploadBackgroundLocation(workspaceId, boardId),
            contentResolver,
            uri,
            boardId
        )
    }



    companion object{
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()
        @Volatile
        private lateinit var instance: StorageHelper
        fun getInstance(): StorageHelper{
            if(::instance.isInitialized.not()){
                instance = StorageHelper()
            }
            return instance
        }

        const val ATTACHMENT_ROOT = "attachments"
        const val BACKGROUND_ROOT = "backgrounds"
        const val COVER_ROOT = "covers"
        const val AVATAR_ROOT = "avatars"
    }
}