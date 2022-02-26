package com.tnh.mollert.home

import android.content.Context
import android.content.res.Resources
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.R
import com.tnh.mollert.boardDetail.SearchCardAdapter
import com.tnh.mollert.databinding.SearchLayoutBinding
import com.tnh.tnhlibrary.dataBinding.recycler.SimpleClickableDataBindingListAdapter
import com.tnh.tnhlibrary.view.show

class SearchDialog(context: Context, container: ViewGroup?): BottomSheetDialog(context) {
    val binding = SearchLayoutBinding.inflate(layoutInflater, container, false)
    init {
        binding.root.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
        behavior.isDraggable = false
        binding.searchLayoutToolbar.apply {
            twoActionToolbarTitle.text = "Search results"
            twoActionToolbarStartIcon.show()
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.setOnClickListener {
                dismiss()
            }
        }
        setContentView(binding.root)
    }

    fun setBoardAdapter(adapter: SearchBoardAdapter){
        binding.searchLayoutRecycler.adapter = adapter
    }

    fun setCardAdapter(adapter: SearchCardAdapter){
        binding.searchLayoutRecycler.adapter = adapter
    }

    override fun show() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        super.show()
    }

}