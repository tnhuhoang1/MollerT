package com.tnh.mollert.boardDetail

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.R
import com.tnh.mollert.databinding.BoardDescBinding
import com.tnh.tnhlibrary.view.show

class DescriptionDialog(
    context: Context,
    container: ViewGroup?
): BottomSheetDialog(context) {
    var onCreateClick: ((desc: String) -> Unit)? = null

    val binding =
        BoardDescBinding.inflate(LayoutInflater.from(context), container, false)
    init {
        binding.root.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
        binding.boardDescToolbar.apply {
            twoActionToolbarTitle.text = "Description"
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_tick)
            twoActionToolbarEndIcon.show()

            twoActionToolbarStartIcon.setOnClickListener {
                dismiss()
            }

            twoActionToolbarEndIcon.setOnClickListener {
                onCreateClick?.invoke(binding.boardDescDesc.text.toString())
                dismiss()
            }
        }
        setContentView(binding.root)
    }

    fun setHint(hint: String){
        binding.boardDescDesc.hint = hint
    }

    fun showFullscreen(content: String?){
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.boardDescDesc.setText(content)
        show()
    }
}