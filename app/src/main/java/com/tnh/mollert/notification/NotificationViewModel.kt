package com.tnh.mollert.notification

import androidx.lifecycle.asLiveData
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: AppRepository
): BaseViewModel() {

    val memberAndActivity = repository.appDao.getActivityAssocWithEmail(UserWrapper.getInstance()?.currentUserEmail ?: "").asLiveData()
}