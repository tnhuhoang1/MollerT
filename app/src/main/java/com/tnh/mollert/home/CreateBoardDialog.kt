package com.tnh.mollert.home

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.tnh.mollert.R
import com.tnh.mollert.databinding.CreateBoardDialogBinding
import com.tnh.mollert.utils.BackgroundPreset
import com.tnh.tnhlibrary.view.show

class CreateBoardDialog(
    context: Context,
    container: ViewGroup?
): BottomSheetDialog(context) {
    val binding = CreateBoardDialogBinding.inflate(LayoutInflater.from(context), container, false)
    private var selectedTv: MaterialTextView? = null
    private val adapter = BackgroundAdapter()

    /**
     * vis will return "null" if no item selected
     */
    var onConfirmClicked: (name: String, vis: String?, url: String?) -> Unit = {_, _, _->}

    init {
        binding.root.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
        binding.createBoardDialogToolbar.apply {
            twoActionToolbarTitle.text = "Create board"
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()

            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_tick)
            twoActionToolbarEndIcon.show()

            twoActionToolbarStartIcon.setOnClickListener {
                dismiss()
            }

            twoActionToolbarEndIcon.setOnClickListener {
                onConfirmClicked(binding.createBoardDialogName.text.toString(),
                    selectedTv?.text.toString().lowercase(), adapter.selectedLink)
            }
        }

        binding.createBoardDialogPublic.setOnClickListener {
            selectItem(binding.createBoardDialogPublic)
        }

        binding.createBoardDialogWorkspace.setOnClickListener {
            selectItem(binding.createBoardDialogWorkspace)
        }

        binding.createBoardDialogPrivate.setOnClickListener {
            selectItem(binding.createBoardDialogPrivate)
        }

        binding.createBoardDialogRecycler.adapter = adapter
        adapter.submitList(BackgroundPreset.backgrounds)
        behavior.isDraggable = false
        setContentView(binding.root)
    }


    private fun selectItem(textView: MaterialTextView){
        selectedTv?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_circle_default, 0, 0, 0)
        selectedTv = textView
        selectedTv?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_checked, 0, 0, 0)
    }

    fun refresh(){
        binding.createBoardDialogName.setText("")
        selectedTv?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_circle_default, 0, 0, 0)
        selectedTv = null
        adapter.clearSelected()
        adapter.submitList(BackgroundPreset.backgrounds)
    }

    override fun show() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        super.show()
    }

}