package com.tnh.mollert.profile.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper,
    private val storage: StorageHelper
) : BaseViewModel() {
    private var _member = MutableLiveData<Member?>(null)
    val member = _member.toLiveData()

    fun getMemberInfoByEmail() {
        viewModelScope.launch {
            val currentUser = UserWrapper.getInstance()?.getCurrentUser()
            if (currentUser != null) {
                _member.postValue(currentUser)
            }
        }
    }

    fun saveMemberInfoToFirestore(member: RemoteMember) {
        viewModelScope.launch {
            val doc = firestore.getMemberDoc(member.email!!)
            firestore.addDocument(doc, member, {
                // failure
                trace(it)
            }) {
                // success
                registerTracking(member.email!!)
            }
        }
    }

    private fun registerTracking(email: String) {
        firestore.mergeDocument(
            firestore.getTrackingDoc(email),
            mapOf<String, List<String>>("workspaces" to listOf("hello world")),
            {}
        ) {

        }
    }

    companion object {
        const val EVENT_PROFILE_IMAGE_CLICKED = "profile_image_clicked"
    }
}