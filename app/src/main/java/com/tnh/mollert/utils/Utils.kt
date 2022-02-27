package com.tnh.mollert.utils

import android.content.res.Resources
import android.util.DisplayMetrics
import com.tnh.mollert.datasource.AppRepository
import com.tnh.tnhlibrary.logAny
import kotlinx.coroutines.CancellableContinuation
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

val Int.pxToDp
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.dpToPx
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Long.getDate(format: String = "dd/MM/yyyy"): String{
    return SimpleDateFormat(format, Locale.getDefault()).format(this)
}

inline fun<T> CancellableContinuation<T>.safeResume(block: () -> T){
    if(isActive){
        resume(block())
    }
}

suspend fun notifyBoardMember(repository: AppRepository, firestore: FirestoreHelper, boardId: String, field: String, data: Any){
    repository.appDao.getBoardWithMembers(boardId)?.members?.let { listMember->
        listMember.forEach { mem->
            firestore.insertToArrayField(
                firestore.getTrackingDoc(mem.email),
                field,
                data
            )
        }
    }
}