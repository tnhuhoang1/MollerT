package com.tnh.mollert.cardDetail

import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import com.tnh.mollert.databinding.LoginFragmentBinding
@AndroidEntryPoint
class CardDetailFragment: DataBindingFragment<LoginFragmentBinding>(R.layout.login_fragment) {
    val viewModel by viewModels<CardDetailFragmentViewModel>()
}