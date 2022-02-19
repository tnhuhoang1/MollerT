package com.tnh.mollert.register

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.R
import com.tnh.mollert.databinding.RegisterFragmentBinding
import com.tnh.mollert.utils.LoadingModal
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment: DataBindingFragment<RegisterFragmentBinding>(R.layout.register_fragment) {
    val viewModel by viewModels<RegisterFragmentViewModel>()
    private val loadingModal by lazy{
        LoadingModal(requireContext())
    }
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
        val email = binding.registerFragmentEmail.text.toString().trim()
        val password = binding.registerFragmentPassword.text.toString().trim()
        val confirmPass = binding.registerFragmentConfirmPassword.text.toString().trim()

        if (password != confirmPass) {
            binding.root.showSnackBar("Password and confirm password need to be equal")
            return false
        }

        if (!ValidationHelper.getInstance().isValidEmail(email)) {
            binding.root.showSnackBar("Email invalid, please try again")
            return false
        }

        if (!ValidationHelper.getInstance().isValidPassword(password)) {
            binding.root.showSnackBar("Password invalid, please try again")
            return false
        }
        return true
    }

    private fun onCreateAccountClicked() {
        if (!this.isValidInput()) {
            this.clearInputText()
            return
        }
        loadingModal.show()
        activity?.let {
            val password = binding.registerFragmentPassword.text.toString()
            val email = binding.registerFragmentEmail.text.toString()
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                }
                .addOnCompleteListener(it) {

                    // TODO: For testing purpose, the present for Thao

                    // Store current user into Firestore
                    lifecycleScope.launchWhenCreated {
                        viewModel.storeCurrentUserToFirestore(email)
                    }
                    // Navigate to Home
                    if (auth.currentUser != null) {
                        binding.root.showSnackBar("Welcome!")
                        this.navigateToHome()
                    }
                    loadingModal.dismiss()
                }
                .addOnFailureListener { e->
                    trace(e)
                    binding.root.showSnackBar("Something went wrong, please try again")
                    loadingModal.dismiss()
                }
        } ?: loadingModal.dismiss()
    }
}