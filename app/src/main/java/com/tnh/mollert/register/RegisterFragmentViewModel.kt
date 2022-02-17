package com.tnh.mollert.register

import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterFragmentViewModel @Inject constructor(
    private val firestore: FirestoreHelper
): BaseViewModel() {

    fun storeCurrentUserToFirestore(email: String) {
        val member = RemoteMember(email, email, "", "", listOf())
        val doc = firestore.getMemberDoc(email)
        firestore.addDocument(doc, member, {
            // failure
            trace(it)
        }) {
            // success
            registerTracking(email)
        }
    }

    private fun registerTracking(email: String){
        firestore.mergeDocument(
            firestore.getTrackingDoc(email),
            mapOf<String, List<String>>("workspaces" to listOf("hello world")),
            {}
        ){

        }
    }

    companion object {

        const val EVENT_LOGIN_CLICKED = "login_clicked"
        const val EVENT_BACK_CLICKED = "back_clicked"
        const val EVENT_REG_CLICKED = "reg_clicked"
    }
}