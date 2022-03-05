package com.tnh.mollert.home.manage

import android.content.Context
import android.content.res.Resources
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tnh.mollert.R
import com.tnh.mollert.databinding.ProfileFragmentBinding
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.utils.bindImageUri
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show

class MemberDialog(context: Context, container: ViewGroup?): BottomSheetDialog(context) {
    val binding = ProfileFragmentBinding.inflate(layoutInflater, container, false)

    init {
        behavior.isDraggable = false
        binding.root.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
        binding.profileFragmentToolbar.apply {
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()
            twoActionToolbarStartIcon.setOnClickListener {
                dismiss()
            }
        }
        setContentView(binding.root)
    }

    override fun show() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        super.show()
    }

    fun showMember(member: Member){
        bindData(member)
        show()
    }

    private fun bindData(member: Member){
        binding.apply {
            member.logAny()
            profileFragmentProfileImage.bindImageUri(member.avatar)
            profileFragmentProfileName.text = member.name
            profileFragmentEmail.setText(member.email)
            profileFragmentBio.setText(member.biography)
            profileFragmentPassword.gone()
            profileFragmentPasswordLabel.gone()
            profileFragmentLogOut.gone()
        }
    }

}