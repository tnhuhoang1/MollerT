package com.tnh.mollert.utils

import android.text.InputFilter
import android.text.Spanned

class SpecialCharFilter: InputFilter {
    private val blockChars = "~#^|$%&*~%-+,/?}{()[`]="
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        if(source != null && blockChars.contains(source)){
            return ""
        }
        return source ?: ""
    }
}