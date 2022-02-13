package com.tnh.mollert.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tnh.mollert.datasource.AppRepository
import com.tnh.tnhlibrary.liveData.EventLiveData
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashFragmentViewModel @Inject constructor(
    private val repository: AppRepository
): BaseViewModel() {

    companion object{
        const val EVENT_SIGN_IN_CLICKED = "sign_in"
        const val EVENT_SIGN_UP_CLICKED = "sign_up"
    }
}


