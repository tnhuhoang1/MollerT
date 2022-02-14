package com.tnh.mollert.register

import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterFragmentViewModel @Inject constructor(
    private val reposiory: AppRepository
): BaseViewModel() {

    fun storeCurrentUserToFirestore(email: String, password: String) {
        val member = RemoteMember(email)
        val doc = FirestoreHelper.getInstance().getMemberDoc(email)
        FirestoreHelper.getInstance().addDocument(doc, member, {
            // Success
        }) {
            // Failure
        }
    }

    companion object {

        const val EVENT_LOGIN_CLICKED = "login_clicked"
        const val EVENT_BACK_CLICKED = "back_clicked"
        const val EVENT_REG_CLICKED = "reg_clicked"
    }
}