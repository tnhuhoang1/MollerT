package com.tnh.mollert.cardDetail.label

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.R
import com.tnh.mollert.databinding.LabelPickerBinding
import com.tnh.mollert.datasource.local.model.Label
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.logVar
import com.tnh.tnhlibrary.view.show

class LabelPickerDialog(
    context: Context,
    container: ViewGroup?
): BottomSheetDialog(context) {
    var onCreateClick: (() -> Unit)? = null
    var onEditLabelClicked: ((labelId: String, name: String) -> Unit)? = null
    private val adapter = LabelAdapter()

    fun setEditLabelListener(listener: (String, name: String) -> Unit){
        onEditLabelClicked = listener
        adapter.onEditClicked = onEditLabelClicked
    }

    val binding: LabelPickerBinding =
        LabelPickerBinding.inflate(LayoutInflater.from(context), container, false)
    init {
        binding.labelPickerToolbar.twoActionToolbarTitle.text = "Labels"
        binding.root.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
        binding.labelPickerToolbar.apply {
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_tick)
            twoActionToolbarEndIcon.show()
            binding.labelItemRecycler.adapter = adapter
            twoActionToolbarStartIcon.setOnClickListener {
                dismiss()
            }
            twoActionToolbarEndIcon.setOnClickListener {

            }
        }
        binding.labelPickerNewLabel.setOnClickListener{
            onCreateClick?.invoke()
            dismiss()
        }
        setContentView(binding.root)
    }

    fun submitList(list: List<Label>){
        adapter.submitList(list)
    }

    fun showFullscreen(){
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        show()
    }
}