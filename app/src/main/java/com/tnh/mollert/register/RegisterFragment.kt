package com.tnh.mollert.register

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tnh.mollert.R
import com.tnh.mollert.databinding.RegisterFragmentBinding
import com.tnh.mollert.utils.LoadingModal
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
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

    private fun navigateToLogin() {
        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
    }

    private fun navigateToHome() {
        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToHomeFragment())
    }

    private fun onCreateAccountClicked() {
        val email = binding.registerFragmentEmail.text.toString().trim()
        val password = binding.registerFragmentPassword.text.toString().trim()
        val confirmPass = binding.registerFragmentConfirmPassword.text.toString().trim()
        if(viewModel.checkInput(email, password, confirmPass).not()){
            return
        }
        viewModel.register(email, password){
            binding.root.showSnackBar("Welcome!")
            this.navigateToHome()
        }
    }
}