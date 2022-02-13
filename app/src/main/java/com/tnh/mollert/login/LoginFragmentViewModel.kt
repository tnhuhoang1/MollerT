package com.tnh.mollert.login

import androidx.lifecycle.ViewModel
import com.tnh.mollert.datasource.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginFragmentViewModel @Inject constructor(
    private val reposiory: AppRepository
): ViewModel() {
}