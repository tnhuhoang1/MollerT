package com.tnh.mollert.forgotpassword

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import javax.inject.Inject

class ForgotPasswordViewModel @Inject constructor(

) : BaseViewModel() {
    private val _progress = MutableLiveData(false)
    val progress = _progress.toLiveData()

    fun showProgress(){
        _progress.postValue(true)
    }

    fun hideProgress(){
        _progress.postValue(false)
    }

    fun forgotPassword(email: String, onSuccess: ()-> Unit) {
        if (!ValidationHelper.getInstance().isValidEmail(email)) {
            postMessage("Email invalid, please try again")
            return
        }
        showProgress()
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener {
            hideProgress()
            postMessage("Reset password successfully, please check your email")
            onSuccess()
        }.addOnFailureListener {
            hideProgress()
            it.message?.let { message->
                when{
                    message.contains("no user record")->{
                        postMessage("This email isn’t linked to any account")
                    }
                    message.contains("network error")->{
                        postMessage("You are offline")
                    }
                    else->{
                        postMessage(message)
                    }
                }
            }
        }
    }

    companion object {
        const val EVENT_SEND_PASSWORD_CLICKED = "send_password_clicked"
        const val EVENT_BACK_CLICKED = "back_clicked"
        const val EVENT_SIGN_UP_CLICKED = "sign_up_clicked"
    }
}