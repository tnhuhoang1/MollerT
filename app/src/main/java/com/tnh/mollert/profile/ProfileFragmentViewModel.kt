package com.tnh.mollert.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileFragmentViewModel @Inject constructor(
    private val repository: AppRepository
): BaseViewModel() {

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

    companion object {
        const val EVENT_LOGOUT_CLICKED = "logout_clicked"
    }
}