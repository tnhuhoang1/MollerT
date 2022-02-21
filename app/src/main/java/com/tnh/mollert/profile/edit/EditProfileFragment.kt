package com.tnh.mollert.profile.edit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.R
import com.tnh.mollert.databinding.EditProfileFragmentBinding
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.profile.ProfileViewModel
import com.tnh.mollert.utils.LoadingModal
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.logVar
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.ActorScope

@AndroidEntryPoint
class EditProfileFragment :
    DataBindingFragment<EditProfileFragmentBinding>(R.layout.edit_profile_fragment) {
    private val viewModel by activityViewModels<ProfileViewModel>()
    private var isChangeImageProfile : Boolean = false

    override fun doOnCreateView() {
        binding.editProfileFragmentToolbar.apply {
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_tick)
            twoActionToolbarEndIcon.show()
            twoActionToolbarEndIcon.setOnClickListener { onSaveButtonClicked() }
            twoActionToolbarStartIcon.setImageResource(R.drawable.vd_close_circle)
            twoActionToolbarStartIcon.show()
            twoActionToolbarStartIcon.setOnClickListener { navigateToProfile() }
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        this.isChangeImageProfile = false

        this.addObserver()
    }

    private fun addObserver() {
        eventObserve(viewModel.clickEvent) { event ->
            when (event) {
                ProfileViewModel.EVENT_PROFILE_IMAGE_CLICKED -> {
                    this.onChangeImageClicked()
                }

                ProfileViewModel.EVENT_SUCCESS -> {
                    navigateToProfile()
                }

            }
        }

        eventObserve(viewModel.message) {
            binding.root.showSnackBar(it)
        }

        safeObserve(viewModel.member) {
            viewModel.setImageUri(it?.avatar ?: "")
        }

    }

    private fun onSaveButtonClicked() {
        if (!this.isValidInput()) {
            return
        }

        val bio = binding.editProfileFragmentBio.text.toString()
        val name = binding.editProfileFragmentName.text.toString()
        val newPassword = binding.editProfileFragmentPassword.text.toString()
        val oldPassword = binding.editProfileFragmentOldPassword.text.toString()

        // Change password
        if (newPassword.isNotEmpty() && oldPassword.isNotEmpty()) {
            viewModel.changePassword(oldPassword, newPassword)
        }

        // Save user info
        viewModel.saveMemberInfoToFirestore(name, bio, requireContext().contentResolver)

        this.navigateToProfile()
    }

    private fun onChangeImageClicked() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        resultLauncher.launch(gallery)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = it.data?.data
                imageUri?.let { _imageUri ->
                    viewModel.setImageUri(_imageUri.toString())
                }
            }
        }

    private fun navigateToProfile() {
        findNavController().navigateUp()
    }

    private fun isValidInput(): Boolean {
        if (binding.editProfileFragmentName.text.toString().isEmpty()) {
            viewModel.setMessage("Profile name invalid, please try again")
            return false
        }

        val password = binding.editProfileFragmentOldPassword.text.toString()
        val oldPassword = binding.editProfileFragmentPassword.text.toString()

        // If user change password
        if (password.isNotEmpty()) {
            if (!ValidationHelper.getInstance().isValidPassword(password)) {
                viewModel.setMessage("Password invalid, please try again")
                return false
            }
        }

        if (oldPassword.isNotEmpty()) {
            if (!ValidationHelper.getInstance().isValidPassword(oldPassword)) {
                viewModel.setMessage("Old Password invalid, please try again")
                return false
            }
        }
        return true
    }
}