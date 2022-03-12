package com.tnh.mollert.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tnh.mollert.R
import com.tnh.mollert.databinding.LoginFragmentBinding
import com.tnh.mollert.utils.LoadingModal
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment: DataBindingFragment<LoginFragmentBinding>(R.layout.login_fragment) {
    val viewModel by viewModels<LoginFragmentViewModel>()
    private val loadingModal by lazy {
        LoadingModal(requireContext())
    }
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
                    this.onLoginBtnClicked()
                }
                LoginFragmentViewModel.EVENT_FORGOT_CLICKED->{
                    this.navigateToForgotPassword()
                }
            }
        }

        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }

        safeObserve(viewModel.progress){
            if(it){
                loadingModal.show()
            }else{
                loadingModal.dismiss()
            }
        }
    }

    private fun navigateToRegister() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
    }

    private fun navigateToHome() {

        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
    }

    private fun navigateToForgotPassword() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
    }

    private fun onLoginBtnClicked() {
        val password = binding.loginFragmentPassword.text.toString().trim()
        val email = binding.loginFragmentEmail.text.toString().trim()
        if(!viewModel.isValidInput(email, password)){
            return
        }
        viewModel.login(email, password){
            this.navigateToHome()
        }
    }
}