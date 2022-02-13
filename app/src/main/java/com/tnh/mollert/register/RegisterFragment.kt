package com.tnh.mollert.register

import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.mollert.databinding.RegisterFragmentBinding

class RegisterFragment: DataBindingFragment<RegisterFragmentBinding>(R.layout.register_fragment) {
    val viewModel by viewModels<RegisterFragmentViewModel>()
}