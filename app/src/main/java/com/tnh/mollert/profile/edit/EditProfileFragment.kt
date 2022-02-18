package com.tnh.mollert.profile.edit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.tnh.mollert.R
import com.tnh.mollert.databinding.EditProfileFragmentBinding
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.dataBinding.DataBindingFragment
import com.tnh.tnhlibrary.liveData.utils.eventObserve
import com.tnh.tnhlibrary.liveData.utils.safeObserve
import com.tnh.tnhlibrary.view.show
import com.tnh.tnhlibrary.view.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.ActorScope

@AndroidEntryPoint
class EditProfileFragment :
    DataBindingFragment<EditProfileFragmentBinding>(R.layout.edit_profile_fragment) {
    private val viewModel by viewModels<EditProfileViewModel>()
    private var isChangeImageProfile : Boolean = false

    override fun doOnCreateView() {
        binding.editProfileFragmentToolbar.apply {
            twoActionToolbarEndIcon.setImageResource(R.drawable.vd_tick)
            twoActionToolbarEndIcon.show()
            twoActionToolbarEndIcon.setOnClickListener {
                onSaveButtonClicked()
            }
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        this.isChangeImageProfile = false

        this.addObserver()
        viewModel.getMemberInfoByEmail()
    }

    private fun addObserver() {
        eventObserve(viewModel.clickEvent) { event ->
            when (event) {
                EditProfileViewModel.EVENT_PROFILE_IMAGE_CLICKED -> {
                    this.onChangeImageClicked()
                }
            }
        }

        safeObserve(viewModel.member) {
            binding.editProfileFragmentName.setText(it?.name)
            binding.editProfileFragmentEmail.setText(it?.email)
            binding.editProfileFragmentBio.setText(it?.biography)
            Glide.with(binding.root)
                .load(it?.avatar)
                .placeholder(R.drawable.app_icon)
                .into(binding.editProfileFragmentProfileImage)
        }
    }

    private fun onSaveButtonClicked() {
        if (!this.isValidInput()) {
            return
        }

        val email = binding.editProfileFragmentEmail.text.toString()
        val bio = binding.editProfileFragmentBio.text.toString()
        val name = binding.editProfileFragmentName.text.toString()
        val member = RemoteMember(email, name, null, bio)

        viewModel.saveMemberInfoToFirestore(member)
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
                    activity?.runOnUiThread {
                        this.isChangeImageProfile = true
                        binding.editProfileFragmentProfileImage.setImageURI(_imageUri)
                        binding.editProfileFragmentProfileImage.invalidate()
                    }
                }
            }
        }

    private fun navigateToProfile() {
        findNavController().navigateUp()
    }

    private fun isValidInput(): Boolean {
        if (binding.editProfileFragmentName.text.toString().isEmpty()) {
            binding.root.showSnackBar("Profile name invalid, please try again")
            return false
        }

        val password = binding.editProfileFragmentOldPassword.text.toString()
        val oldPassword = binding.editProfileFragmentPassword.text.toString()

        // If user change password
        if (password.isNotEmpty()) {
            if (!ValidationHelper.getInstance().isValidPassword(password)) {
                binding.root.showSnackBar("Password invalid, please try again")
                return false
            }
        }

        if (oldPassword.isNotEmpty()) {
            if (!ValidationHelper.getInstance().isValidPassword(oldPassword)) {
                binding.root.showSnackBar("Old Password invalid, please try again")
                return false
            }
        }
        return true
    }
}