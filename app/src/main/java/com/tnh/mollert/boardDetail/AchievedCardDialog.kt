package com.tnh.mollert.boardDetail

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.R
import com.tnh.mollert.databinding.AchievedCardDialogBinding
import com.tnh.mollert.databinding.BoardDescBinding
import com.tnh.mollert.datasource.local.model.Card
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show

class AchievedCardDialog(
    context: Context,
    container: ViewGroup?
): BottomSheetDialog(context) {
    val adapter = BoardDetailAdapter.BoardDetailCardAdapter()


    val binding =
        AchievedCardDialogBinding.inflate(LayoutInflater.from(context), container, false)
    init {
        binding.root.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
        binding.achievedCardDialogToolbar.apply {
            twoActionToolbarTitle.text = "Achieved card"
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()

            twoActionToolbarStartIcon.setOnClickListener {
                dismiss()
            }
        }
        binding.achievedCardDialogRecycler.adapter = adapter
        setContentView(binding.root)
    }

    fun setOnCardClicked(onCardClicked: (listId: String, cardId: String) -> Unit){
        adapter.onCardClicked = onCardClicked
    }

    fun setTitle(title: String){
        binding.achievedCardDialogToolbar.twoActionToolbarTitle.text = title
    }

    fun submitList(list: List<Card>){
        if(list.isEmpty()){
            binding.achievedCardDialogNoContent.show()
            binding.achievedCardDialogRecycler.gone()
        }else{
            binding.achievedCardDialogNoContent.gone()
            binding.achievedCardDialogRecycler.show()
        }
        adapter.submitList(list)
    }

    fun showFullscreen(){
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        show()
    }
}