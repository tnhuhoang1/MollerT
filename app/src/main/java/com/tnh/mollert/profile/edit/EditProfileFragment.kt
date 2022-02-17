package com.tnh.mollert.profile.edit

import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.mollert.databinding.EditProfileFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.view.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment: DataBindingFragment<EditProfileFragmentBinding>(R.layout.edit_profile_fragment) {
    private val  viewModel by viewModels<EditProfileViewModel>()

    override fun doOnCreateView() {
        binding.editProfileFragmentToolbar.apply {
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_tick)
            twoActionToolbarEndIcon.show()
            twoActionToolbarEndIcon.setOnClickListener {
                // called when user click confirm



            }
        }
    }
}