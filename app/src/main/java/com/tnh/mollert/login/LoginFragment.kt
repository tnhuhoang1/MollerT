package com.tnh.mollert.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tnh.mollert.R
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import com.tnh.mollert.databinding.LoginFragmentBinding
import com.tnh.tnhlibrary.liveData.utils.eventObserve

@AndroidEntryPoint
class LoginFragment: DataBindingFragment<LoginFragmentBinding>(R.layout.login_fragment) {
    val viewModel by viewModels<LoginFragmentViewModel>()

    override fun doOnCreateView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventObserve(viewModel.clickEvent){event->
            when(event){
                LoginFragmentViewModel.EVENT_BACK_CLICKED->{
                    findNavController().navigateUp()
                }
                LoginFragmentViewModel.EVENT_REG_CLICKED->{
                    navigateToRegister()
                }
                LoginFragmentViewModel.EVENT_LOGIN_CLICKED->{
                    navigateToHome()
                }
            }
        }
    }

    private fun navigateToRegister() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
    }

    private fun navigateToHome(){
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
    }

}