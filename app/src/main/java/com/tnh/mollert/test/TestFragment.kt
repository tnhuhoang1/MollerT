package com.tnh.mollert.test

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tnh.mollert.R
import com.tnh.mollert.databinding.TestFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestFragment: DataBindingFragment<TestFragmentBinding>(R.layout.test_fragment) {
    private val viewModel by viewModels<TestFragmentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}