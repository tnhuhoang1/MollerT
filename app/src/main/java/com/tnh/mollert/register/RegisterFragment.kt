package com.tnh.mollert.register

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tnh.mollert.R
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.mollert.databinding.RegisterFragmentBinding
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment: DataBindingFragment<RegisterFragmentBinding>(R.layout.register_fragment) {
    val viewModel by viewModels<RegisterFragmentViewModel>()

    override fun doOnCreateView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventObserve(viewModel.clickEvent){event->
            when(event){
                RegisterFragmentViewModel.EVENT_BACK_CLICKED->{
                    findNavController().navigateUp()
                }
                RegisterFragmentViewModel.EVENT_LOGIN_CLICKED->{
                    navigateToLogin()
                }
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
    }
}