package com.tnh.mollert.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.tnh.mollert.R
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import com.tnh.mollert.databinding.LoginFragmentBinding
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.toast.showToast

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
                    this.onLoginBtnClicked()
                }
            }
        }
    }

    private fun navigateToRegister() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
    }

    private fun navigateToHome() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
    }

    private fun clearInputText() {
        binding.loginFragmentEmail.setText("")
        binding.loginFragmentEmail.setText("")
    }

    private fun isValidInput(): Boolean {
        val password = binding.loginFragmentPassword.text.toString()
        val email = binding.loginFragmentEmail.text.toString()
        if (!ValidationHelper.getInstance().isValidPassword(password)
            || !ValidationHelper.getInstance().isValidEmail(email)
        ) {
            this.clearInputText()
            showToast("Email or password invalid, please try again")
            return false
        }
        return true
    }

    private fun onLoginBtnClicked() {
        if (!this.isValidInput()) {
            return
        }

        activity?.let {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                binding.loginFragmentEmail.text.toString(),
                binding.loginFragmentPassword.text.toString()
            )
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Login Success
                        this.navigateToHome()
                    } else {
                        this.clearInputText()
                        showToast("Email or password invalid, please try again")
                        // Login failure
                    }
                }
        }
    }
}