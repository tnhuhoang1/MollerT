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
import com.tnh.mollert.utils.SpecialCharFilter
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show

class CreateBoardDialog(
    context: Context,
    container: ViewGroup?
): BottomSheetDialog(context) {
    val binding = CreateBoardDialogBinding.inflate(LayoutInflater.from(context), container, false)
    private var selectedTv: MaterialTextView? = null
    private val adapter = BackgroundAdapter()
    var backgroundMode = BACKGROUND_MODE_DEFAULT

    var onSelectImageClicked: ()-> Unit = {}


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
        binding.createBoardDialogSelectImage.setOnClickListener {
            onSelectImageClicked()
        }
        binding.createBoardDialogName.filters = arrayOf(SpecialCharFilter())
        binding.createBoardDialogPublic.setOnClickListener {
            selectItem(binding.createBoardDialogPublic)
        }

//        binding.createBoardDialogWorkspace.setOnClickListener {
//            selectItem(binding.createBoardDialogWorkspace)
//        }

        binding.createBoardDialogPrivate.setOnClickListener {
            selectItem(binding.createBoardDialogPrivate)
        }

        binding.createBoardDialogRecycler.adapter = adapter
        adapter.onBackgroundSelected = {
            binding.createBoardDialogBackground.text = "Background"
            backgroundMode = BACKGROUND_MODE_DEFAULT
        }
        adapter.submitList(BackgroundPreset.backgrounds)
        behavior.isDraggable = false
        setContentView(binding.root)
    }

    fun setCustomImage(uri: String){
        backgroundMode = BACKGROUND_MODE_CUSTOM
        binding.createBoardDialogBackground.text = uri
        adapter.clearSelected()
        adapter.setSelectedUri(uri)
    }

    private fun selectItem(textView: MaterialTextView){
        selectedTv?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_circle_default, 0, 0, 0)
        selectedTv = textView
        selectedTv?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_checked, 0, 0, 0)
    }

    fun setTitle(title: String){
        binding.createBoardDialogToolbar.twoActionToolbarTitle.text = title
    }

    fun hideNameAndVisibility(){
        binding.createBoardDialogImage.gone()
        binding.createBoardDialogContainer.gone()
        binding.createBoardDialogName.gone()
    }

    fun refresh(){
        binding.createBoardDialogName.setText("")
        selectedTv?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.vd_circle_default, 0, 0, 0)
        selectedTv = null
        adapter.clearSelected()
        adapter.submitList(BackgroundPreset.backgrounds)
        adapter.notifyItemChanged(0)
    }

    override fun show() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        super.show()
    }

    companion object{
        const val BACKGROUND_MODE_DEFAULT = "default"
        const val BACKGROUND_MODE_CUSTOM = "custom"
    }
}