package com.tnh.mollert.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.R
import com.tnh.mollert.databinding.SplashFragmentBinding
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.toast.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : DataBindingFragment<SplashFragmentBinding>(R.layout.splash_fragment) {
    private lateinit var splashAdapter: SplashAdapter
    private val viewModel by viewModels<SplashFragmentViewModel>()
    @Inject
    lateinit var pref: PrefManager
    override fun doOnCreateView() {
//        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToTestFragment())
//        FirebaseAuth.getInstance().signInWithEmailAndPassword(
//            "hoang@gmail.com",
//            "1234567"
//        )
        FirebaseAuth.getInstance().signOut()
        if(viewModel.isUserLoggedIn()){
            navigateToHome()
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventObserve(viewModel.message){
            showToast(it)
        }
        eventObserve(viewModel.clickEvent){
            when(it){
                SplashFragmentViewModel.EVENT_SIGN_IN_CLICKED->{
                    navigateToLogin()
                    viewModel.setMessage("Login")
                }
                SplashFragmentViewModel.EVENT_SIGN_UP_CLICKED->{
                    navigateToRegister()
                    viewModel.setMessage("Register")
                }
            }
        }

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

    private fun navigateToLogin(){
        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
    }

    private fun navigateToRegister(){
        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToRegisterFragment())
    }

    private fun navigateToHome(){
        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToHomeFragment())
    }



}