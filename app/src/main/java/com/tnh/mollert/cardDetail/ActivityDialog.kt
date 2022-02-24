package com.tnh.mollert.cardDetail

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.R
import com.tnh.mollert.databinding.ActivityDialogBinding
import com.tnh.mollert.databinding.BoardDescBinding
import com.tnh.mollert.datasource.local.compound.MemberAndActivity
import com.tnh.mollert.notification.NotificationAdapter
import com.tnh.tnhlibrary.view.show

class ActivityDialog(context: Context, container: ViewGroup?): BottomSheetDialog(context) {
    val binding = ActivityDialogBinding.inflate(LayoutInflater.from(context), container, false)
    val adapter = NotificationAdapter()
    init {
        binding.root.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
        binding.activityDialogToolbar.apply {
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()

            twoActionToolbarStartIcon.setOnClickListener {
                dismiss()
            }
        }
        binding.activityDialogRecycler.adapter = adapter
        setContentView(binding.root)
    }

    fun submitList(list: List<MemberAndActivity>){
        adapter.submitList(list)
    }

    fun setTitle(title: String){
        binding.activityDialogToolbar.twoActionToolbarTitle.text = title
    }

    fun showFullscreen(){
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        show()
    }
}