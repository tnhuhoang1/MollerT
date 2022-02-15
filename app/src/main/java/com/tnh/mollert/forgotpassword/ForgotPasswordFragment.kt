package com.tnh.mollert.forgotpassword

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.R
import com.tnh.mollert.databinding.ForgotPasswordFragmentBinding
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.toast.showToast

class ForgotPasswordFragment :
    DataBindingFragment<ForgotPasswordFragmentBinding>(R.layout.forgot_password_fragment) {
    val viewModel by viewModels<ForgotPasswordViewModel>()

    override fun doOnCreateView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventObserve(viewModel.clickEvent) { event ->
            when (event) {
                ForgotPasswordViewModel.EVENT_BACK_CLICKED -> {
                    this.navigateToSignIn()
                }
                ForgotPasswordViewModel.EVENT_SEND_PASSWORD_CLICKED -> {
                    this.onSendEmailClicked()
                }
                ForgotPasswordViewModel.EVENT_SIGN_UP_CLICKED -> {
                    this.navigateToRegister()
                }
            }
        }
    }

    private fun navigateToSignIn() {
        findNavController().navigate(ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToLoginFragment())
    }

    private fun navigateToRegister() {
        findNavController().navigate(ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToRegisterFragment())
    }

    private fun onSendEmailClicked() {
        val email = binding.forgotPasswordFragmentEmail.text.toString()
        if (!ValidationHelper.getInstance().isValidEmail(email)) {
            binding.forgotPasswordFragmentEmail.setText("")
            showToast("Email invalid, please try again")
            return
        }

        activity?.let {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Success
                        showToast("Reset password successfully, please check your email")
                        this.navigateToSignIn()
                    } else {
                        showToast("Something went wrong, please try again or check your internet connection")
                    }
                }
        }
    }
}