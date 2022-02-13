package com.tnh.mollert.cardDetail.label

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.databinding.LabelPickerBinding
import com.tnh.tnhlibrary.logVar

class LabelPickerDialog(
    context: Context,
    container: ViewGroup?
): BottomSheetDialog(context) {
    val binding: LabelPickerBinding =
        LabelPickerBinding.inflate(LayoutInflater.from(context), container, false)
    init {
        binding.labelPickerToolbar.twoActionToolbarTitle.text = "Labels"
        binding.root.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
        setContentView(binding.root)
    }

    fun showFullscreen(){
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        show()
    }
}