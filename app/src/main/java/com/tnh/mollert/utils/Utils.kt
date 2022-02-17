package com.tnh.mollert.utils

import android.content.res.Resources
import android.util.DisplayMetrics
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

val Int.pxToDp
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.dpToPx
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

inline fun<T> CancellableContinuation<T>.safeResume(block: () -> T){
    if(isActive){
        resume(block())
    }
}