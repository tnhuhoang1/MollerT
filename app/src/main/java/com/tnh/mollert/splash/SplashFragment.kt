package com.tnh.mollert.splash

import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tnh.mollert.R
import com.tnh.mollert.databinding.SplashFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : DataBindingFragment<SplashFragmentBinding>(R.layout.splash_fragment) {
    private lateinit var splashAdapter: SplashAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        splashAdapter = SplashAdapter(
            listOf(
                WelcomeFragment(),
                WelcomeFragment(),
                WelcomeFragment(),
                WelcomeFragment(),
            ),
            requireActivity()
        )
        binding.splashFragmentViewPager.adapter = splashAdapter
        TabLayoutMediator(binding.splashFragmentTab, binding.splashFragmentViewPager
        ) { tab, position ->

        }.attach()
    }
}