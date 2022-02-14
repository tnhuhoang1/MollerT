package com.tnh.mollert.splash

import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.AppRepository
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.logVar
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashFragmentViewModel @Inject constructor(
    private val repository: AppRepository
): BaseViewModel() {

    fun isUserLoggedIn(): Boolean{
        return (FirebaseAuth.getInstance().currentUser != null)
    }

    companion object{
        const val EVENT_SIGN_IN_CLICKED = "sign_in"
        const val EVENT_SIGN_UP_CLICKED = "sign_up"
    }
}


