package com.tnh.mollert

import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tnh.mollert.utils.StorageFunction
import com.tnh.tnhlibrary.logAny
import java.io.InputStream
import javax.inject.Inject

class FakeStorage @Inject constructor(): StorageFunction {
    private val s = FirebaseStorage.getInstance()
    override fun getUploadAttachmentLocation(boardId: String, cardId: String): StorageReference {
        return s.getReference("test")
    }

    override fun getUploadBackgroundLocation(
        workspaceId: String,
        boardId: String
    ): StorageReference {
        return s.getReference("test")
    }

    override fun getUploadCoverLocation(cardId: String): StorageReference {
        return s.getReference("test")

    }

    override fun getAvatarLocation(email: String): StorageReference {
        return s.getReference("test")

    }

    override fun getCustomRef(path: String): StorageReference {
        return s.getReference("test")

    }

    override fun getStorage(): FirebaseStorage {
        TODO("Not yet implemented")
    }

    override fun uploadFromStream(
        ref: StorageReference,
        dataStream: InputStream,
        failureBlock: (Exception?) -> Unit,
        successBlock: (url: Uri) -> Unit
    ) {
        "Upload test file successfully".logAny()
    }

    override suspend fun uploadAndGetUrlFromStream(
        ref: StorageReference,
        dataStream: InputStream,
        failureBlock: (Exception?) -> Unit,
        successBlock: (url: Uri) -> Unit
    ): Uri? {
        return "https://baoquocte.vn/stores/news_dataimages/dieulinh/012020/29/15/nhung-buc-anh-dep-tuyet-voi-ve-tinh-ban.jpg".toUri()
    }

    override suspend fun uploadImage(
        parentRef: StorageReference,
        contentResolver: ContentResolver,
        uri: Uri,
        imageName: String
    ): Uri? {
        return "https://baoquocte.vn/stores/news_dataimages/dieulinh/012020/29/15/nhung-buc-anh-dep-tuyet-voi-ve-tinh-ban.jpg".toUri()

    }

    override suspend fun uploadCardCover(
        contentResolver: ContentResolver,
        uri: Uri,
        cardId: String
    ): Uri? {
        return "https://baoquocte.vn/stores/news_dataimages/dieulinh/012020/29/15/nhung-buc-anh-dep-tuyet-voi-ve-tinh-ban.jpg".toUri()

    }

    override suspend fun uploadImageAttachment(
        contentResolver: ContentResolver,
        uri: Uri,
        boardId: String,
        cardId: String,
        attachmentId: String
    ): Uri? {
        return "https://baoquocte.vn/stores/news_dataimages/dieulinh/012020/29/15/nhung-buc-anh-dep-tuyet-voi-ve-tinh-ban.jpg".toUri()

    }

    override suspend fun uploadBackgroundImage(
        workspaceId: String,
        boardId: String,
        contentResolver: ContentResolver,
        uri: Uri
    ): Uri? {
        return "https://baoquocte.vn/stores/news_dataimages/dieulinh/012020/29/15/nhung-buc-anh-dep-tuyet-voi-ve-tinh-ban.jpg".toUri()

    }
}