package com.tnh.mollert.splash

import androidx.lifecycle.ViewModel
import com.tnh.mollert.datasource.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashFragmentViewModel @Inject constructor(
    private val repository: AppRepository
): ViewModel() {
}