package com.tnh.mollert.profile

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tnh.mollert.R
import com.tnh.mollert.databinding.ProfileFragmentBinding
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.preference.PrefManager
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment: DataBindingFragment<ProfileFragmentBinding>(R.layout.profile_fragment) {
    private val viewModel by viewModels<ProfileViewModel>()
    @Inject lateinit var prefManager: PrefManager
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
        binding.lifecycleOwner = viewLifecycleOwner
        this.addObserver()
    }

    private fun navigateToSplash() {
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToSplashFragment())
    }

    private fun navigateToEdit() {
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment())
    }

    private fun addObserver() {
        eventObserve(viewModel.clickEvent) { event ->
            when (event) {
                ProfileViewModel.EVENT_LOGOUT_CLICKED -> {
                    this.onLogoutButtonClicked()
                }
            }
        }
        safeObserve(viewModel.member){ member ->
            member?.let {
                binding.apply {
                    profileFragmentProfileName.text = member.name
                    profileFragmentEmail.setText(member.email)
                    profileFragmentBio.setText(member.biography)
                }
            }
        }
        eventObserve(viewModel.message){
            binding.root.showSnackBar(it)
        }

    }

    private fun onLogoutButtonClicked() {
        lifecycleScope.launchWhenCreated {
            viewModel.logout(prefManager)
            navigateToSplash()
        }
    }
}