package com.tnh.mollert.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.ValidationHelper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class RegisterFragmentViewModel @Inject constructor(
    private val firestore: FirestoreHelper
): BaseViewModel() {
    private val _progress = MutableLiveData(false)
    val progress = _progress.toLiveData()

    fun showProgress(){
        _progress.postValue(true)
    }

    fun hideProgress(){
        _progress.postValue(false)
    }

    fun checkInput(email: String, password: String, passwordConf: String): Boolean{
        if(email.isEmpty() || password.isEmpty() || passwordConf.isEmpty()){
            postMessage("Please fill out the form to continue")
            return false
        }
        if (password != passwordConf) {
            postMessage("Password and confirm password must be equal")
            return false
        }

        if (!ValidationHelper.getInstance().isValidEmail(email)) {
            postMessage("Email invalid, please try again")
            return false
        }

        if (!ValidationHelper.getInstance().isValidPassword(password)) {
            postMessage("Password invalid, please try again")
            return false
        }
        return true
    }

    fun register(
        email: String,
        password: String,
        onSuccess: ()-> Unit
    ){
        viewModelScope.launch {
            withTimeoutOrNull(15000){
                val auth = FirebaseAuth.getInstance()
                showProgress()
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        storeCurrentUserToFirestore(email)
                        // Navigate to Home
                        if (auth.currentUser != null) {
                            hideProgress()
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e->
                        trace(e)
                        e.message?.let { message->
                            when {
                                message.contains("network error") -> {
                                    postMessage("Network error")
                                }
                                message.contains("The email address is already") -> {
                                    postMessage("The email address is already taken")
                                }
                                else -> {
                                    postMessage(message)
                                }
                            }
                        }
                        hideProgress()
                    }
            }?: kotlin.run {
                hideProgress()
                postMessage("Something went wrong, please try again")
            }
        }

    }

    private fun storeCurrentUserToFirestore(email: String) {
        val member = RemoteMember(email, email, "", "", listOf())
        val doc = firestore.getMemberDoc(email)
        firestore.addDocument(doc, member, {
            // failure
            trace(it)
        }) {
            // success
        }
    }

//    private fun registerTracking(email: String){
//        firestore.mergeDocument(
//            firestore.getTrackingDoc(email),
//            mapOf<String, List<String>>("workspaces" to listOf()),
//            {}
//        ){
//
//        }
//    }

    companion object {

        const val EVENT_LOGIN_CLICKED = "login_clicked"
        const val EVENT_BACK_CLICKED = "back_clicked"
        const val EVENT_REG_CLICKED = "reg_clicked"
    }
}