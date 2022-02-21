package com.tnh.mollert.utils

import android.content.res.Resources
import android.util.DisplayMetrics
import kotlinx.coroutines.CancellableContinuation
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

val Int.pxToDp
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.dpToPx
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Long.getDate(): String{
    return SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(this)
}

inline fun<T> CancellableContinuation<T>.safeResume(block: () -> T){
    if(isActive){
        resume(block())
    }
}