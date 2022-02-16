package com.tnh.mollert.utils

import android.content.res.Resources
import android.util.DisplayMetrics

val Int.pxToDp
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.dpToPx
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()