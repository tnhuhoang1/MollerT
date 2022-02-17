package com.tnh.mollert.profile.edit

import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper,
    private val storage: StorageHelper
): BaseViewModel() {
}