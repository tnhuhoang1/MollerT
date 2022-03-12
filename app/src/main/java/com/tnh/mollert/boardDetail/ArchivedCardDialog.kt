package com.tnh.mollert.boardDetail

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.R
import com.tnh.mollert.databinding.ArchivedCardDialogBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show

class ArchivedCardDialog(
    context: Context,
    container: ViewGroup?
): BottomSheetDialog(context) {
    val adapter = BoardDetailAdapter.BoardDetailCardAdapter()


    val binding =
        ArchivedCardDialogBinding.inflate(LayoutInflater.from(context), container, false)
    init {
        binding.root.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
        binding.archivedCardDialogToolbar.apply {
            twoActionToolbarTitle.text = "Archived card"
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()

            twoActionToolbarStartIcon.setOnClickListener {
                dismiss()
            }
        }
        binding.archivedCardDialogRecycler.adapter = adapter
        setContentView(binding.root)
    }

    fun setOnCardClicked(onCardClicked: (listId: String, cardId: String) -> Unit){
        adapter.onCardClicked = onCardClicked
    }

    fun setTitle(title: String){
        binding.archivedCardDialogToolbar.twoActionToolbarTitle.text = title
    }

    fun submitList(list: List<Card>){
        if(list.isEmpty()){
            binding.archivedCardDialogNoContent.show()
            binding.archivedCardDialogRecycler.gone()
        }else{
            binding.archivedCardDialogNoContent.gone()
            binding.archivedCardDialogRecycler.show()
        }
        adapter.submitList(list)
    }

    fun showFullscreen(){
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        show()
    }
}