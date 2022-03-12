package com.tnh.mollert.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class LoginFragmentViewModel @Inject constructor(): BaseViewModel() {

    fun isValidInput(email: String, password: String): Boolean {
        if(email.isEmpty() || password.isEmpty()){
            postMessage("Please fill out the form to continue")
            return false
        }
        if(!ValidationHelper.getInstance().isValidEmail(email)){
            postMessage("Email invalid, please try again")
            return false
        }
        if (!ValidationHelper.getInstance().isValidPassword(password)) {
            postMessage("Password invalid, please try again")
            return false
        }
        return true
    }

    private val _progress = MutableLiveData(false)
    val progress = _progress.toLiveData()

    private fun showProgress(){
        _progress.postValue(true)
    }

    private fun hideProgress(){
        _progress.postValue(false)
    }


    fun login(email: String, password: String, onSuccess: () -> Unit){
        viewModelScope.launch {
            withTimeoutOrNull(15000){
                showProgress()
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        hideProgress()
                        onSuccess()
                    }.addOnFailureListener { e->
                        trace(e)
                        hideProgress()
                        e.message?.let { message->
                            when {
                                message.contains("no user record") -> {
                                    postMessage("This account is not exists")
                                }
                                message.contains("network error") -> {
                                    postMessage("Network error")
                                }
                                else -> {
                                    postMessage("Incorrect password")
                                }
                            }
                        }
                    }
            } ?: kotlin.run {
                hideProgress()
                postMessage("Something went wrong, please try again")
            }
        }
    }

    companion object{
        const val EVENT_LOGIN_CLICKED = "login_clicked"
        const val EVENT_BACK_CLICKED = "back_clicked"
        const val EVENT_REG_CLICKED = "reg_clicked"
        const val EVENT_FORGOT_CLICKED = "forgot_clicked"
    }
}