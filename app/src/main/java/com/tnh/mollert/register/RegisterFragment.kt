package com.tnh.mollert.register

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.R
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.mollert.databinding.RegisterFragmentBinding
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.toast.showToast
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
        eventObserve(viewModel.clickEvent) { event ->
            when (event) {
                RegisterFragmentViewModel.EVENT_BACK_CLICKED -> {
                    findNavController().navigateUp()
                }
                RegisterFragmentViewModel.EVENT_LOGIN_CLICKED -> {
                    navigateToLogin()
                }
                RegisterFragmentViewModel.EVENT_REG_CLICKED -> {
                    this.onCreateAccountClicked()
                }
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
    }

    private fun navigateToHome() {
        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToHomeFragment())
    }

    private fun clearInputText() {
        binding.registerFragmentEmail.setText("")
        binding.registerFragmentPassword.setText("")
        binding.registerFragmentConfirmPassword.setText("")
    }

    private fun isValidInput(): Boolean {
        val email = binding.registerFragmentEmail.text.toString()
        val password = binding.registerFragmentPassword.text.toString()
        val confirmPass = binding.registerFragmentConfirmPassword.text.toString()

        if (password != confirmPass) {
            showToast("Password and confirm password need to be equal")
            return false
        }

        if (!ValidationHelper.getInstance().isValidEmail(email)) {
            showToast("Email invalid, please try again")
            return false
        }

        if (!ValidationHelper.getInstance().isValidPassword(password)) {
            showToast("Password invalid, please try again")
            return false
        }
        return true
    }

    private fun onCreateAccountClicked() {
        if (!this.isValidInput()) {
            this.clearInputText()
            return
        }

        activity?.let {
            val password = binding.registerFragmentPassword.text.toString()
            val email = binding.registerFragmentEmail.text.toString()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(it) {
                    // Store current user into local database
                    lifecycleScope.launchWhenCreated {
                        viewModel.storeCurrentUserToLocal(email, password)
                        viewModel.storeCurrentUserToFirestore(email, password)
                    }

                    // Navigate to Home
                    this.navigateToHome()
                }
        }
    }
}