package com.tnh.mollert.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.R
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import com.tnh.mollert.databinding.LoginFragmentBinding
import com.tnh.mollert.utils.LoadingModal
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.toast.showToast
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.view.snackbar.showSnackBar

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

    private fun clearInputText() {
        binding.loginFragmentEmail.setText("")
        binding.loginFragmentPassword.setText("")
    }

    private fun isValidInput(): Boolean {
        val password = binding.loginFragmentPassword.text.toString().trim()
        val email = binding.loginFragmentEmail.text.toString().trim()
        if (!ValidationHelper.getInstance().isValidPassword(password)
            || !ValidationHelper.getInstance().isValidEmail(email)
        ) {
            this.clearInputText()
            binding.root.showSnackBar("Email or password invalid, please try again")
            return false
        }
        return true
    }

    private fun onLoginBtnClicked() {
        if (!this.isValidInput()) {
            return
        }

        loadingModal.show()

        activity?.let {
            loadingModal.show()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                binding.loginFragmentEmail.text.toString(),
                binding.loginFragmentPassword.text.toString()
            )
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Login Success
                        loadingModal.dismiss()
                        this.navigateToHome()
                    } else {
                        this.clearInputText()
                        binding.root.showSnackBar("Email or password invalid, please try again")
                        loadingModal.dismiss()
                        // Login failure
                    }
                }.addOnFailureListener { e->
                    trace(e)
                }
        }
    }
}