package com.tnh.mollert.utils

import android.content.ContentResolver
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tnh.tnhlibrary.trace
import java.io.IOException
import java.io.InputStream

interface StorageFunction {
    fun getUploadAttachmentLocation(boardId: String, cardId: String): StorageReference
    fun getUploadBackgroundLocation(workspaceId: String, boardId: String): StorageReference
    fun getUploadCoverLocation(cardId: String): StorageReference
    fun getAvatarLocation(email: String): StorageReference
    fun getCustomRef(path: String): StorageReference
    fun getStorage(): FirebaseStorage

    @Throws(IOException::class)
    fun uploadFromStream(
        ref: StorageReference,
        dataStream: InputStream,
        failureBlock: (Exception?) -> Unit,
        successBlock: (url: Uri) -> Unit
    )

    suspend fun uploadAndGetUrlFromStream(
        ref: StorageReference,
        dataStream: InputStream,
        failureBlock: (Exception?) -> Unit = { trace(it) },
        successBlock: (url: Uri) -> Unit = {}
    ): Uri?

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
    ): Uri?

    suspend fun uploadCardCover(
        contentResolver: ContentResolver,
        uri: Uri,
        cardId: String
    ): Uri?

    suspend fun uploadImageAttachment(
        contentResolver: ContentResolver,
        uri: Uri,
        boardId: String,
        cardId: String,
        attachmentId: String,
    ): Uri?

    suspend fun uploadBackgroundImage(
        workspaceId: String,
        boardId: String,
        contentResolver: ContentResolver,
        uri: Uri
    ): Uri?
}