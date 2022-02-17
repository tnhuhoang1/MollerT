package com.tnh.mollert.profile

import com.tnh.mollert.R
import com.tnh.mollert.databinding.ProfileFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.view.show

class ProfileFragment: DataBindingFragment<ProfileFragmentBinding>(R.layout.profile_fragment) {
    override fun doOnCreateView() {
        binding.profileFragmentToolbar.apply {
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_user_edit)
            twoActionToolbarEndIcon.show()
            twoActionToolbarEndIcon.setOnClickListener {
                // called when user click edit button



            }
        }
    }
}