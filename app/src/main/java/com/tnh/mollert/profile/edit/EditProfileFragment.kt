package com.tnh.mollert.profile.edit

import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.mollert.databinding.EditProfileFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment: DataBindingFragment<EditProfileFragmentBinding>(R.layout.edit_profile_fragment) {
    private val  viewModel by viewModels<EditProfileViewModel>()

}