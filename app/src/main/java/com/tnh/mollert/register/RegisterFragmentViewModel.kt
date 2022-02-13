package com.tnh.mollert.register

import androidx.lifecycle.ViewModel
import com.tnh.mollert.datasource.AppRepository
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterFragmentViewModel @Inject constructor(
    private val reposiory: AppRepository
): BaseViewModel() {

    companion object{

        const val EVENT_LOGIN_CLICKED = "login_clicked"
        const val EVENT_BACK_CLICKED = "back_clicked"
        const val EVENT_REG_CLICKED = "reg_clicked"
    }
}