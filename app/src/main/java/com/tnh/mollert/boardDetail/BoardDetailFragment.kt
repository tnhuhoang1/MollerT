package com.tnh.mollert.boardDetail

import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.mollert.databinding.BoardDetailFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BoardDetailFragment: DataBindingFragment<BoardDetailFragmentBinding>(R.layout.login_fragment) {
    val viewModel by viewModels<BoardDetailFragmentViewModel>()
}