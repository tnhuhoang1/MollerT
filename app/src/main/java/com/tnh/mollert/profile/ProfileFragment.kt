package com.tnh.mollert.profile

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.R
import com.tnh.tnhlibrary.view.show
import com.tnh.mollert.databinding.ProfileFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment: DataBindingFragment<ProfileFragmentBinding>(R.layout.profile_fragment) {
    private val  viewModel by viewModels<ProfileFragmentViewModel>()
    override fun doOnCreateView() {
        binding.profileFragmentToolbar.apply {
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_user_edit)
            twoActionToolbarEndIcon.show()
            twoActionToolbarEndIcon.setOnClickListener {
                // called when user click edit button
                navigateToEdit()
            }
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        this.addObserver()
        viewModel.getMemberInfoByEmail()
    }

    private fun navigateToLogin() {
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
    }

    private fun navigateToEdit() {
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment())
    }

    private fun addObserver() {
        eventObserve(viewModel.clickEvent) { event ->
            when (event) {
                ProfileFragmentViewModel.EVENT_LOGOUT_CLICKED -> {
                    this.onLogoutButtonClicked()
                }
            }
        }

        safeObserve(viewModel.member){
            binding.profileFragmentProfileName.text = it?.name
            binding.profileFragmentEmail.setText(it?.email)
            binding.profileFragmentBio.setText(it?.biography)
            Glide.with(binding.root)
                .load(it?.avatar)
                .placeholder(R.drawable.app_icon)
                .into(binding.profileFragmentProfileImage)
        }
    }

    private fun onLogoutButtonClicked() {
        lifecycleScope.launchWhenCreated {
            FirebaseAuth.getInstance().signOut()
            navigateToLogin()
        }
    }
}