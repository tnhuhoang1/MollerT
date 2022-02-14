package com.tnh.mollert.utils

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tnh.mollert.databinding.LoadingLayoutBinding

class LoadingModal(context: Context) {
    val binding = LoadingLayoutBinding.inflate(LayoutInflater.from(context), null, false)
    private val dialog: androidx.appcompat.app.AlertDialog
    init {
        MaterialAlertDialogBuilder(context).apply {
            setCancelable(false)
            setView(binding.root)
            dialog = create()
        }
    }

    fun show(){
        dialog.show()
    }

    fun dismiss(){
        dialog.cancel()
    }
}