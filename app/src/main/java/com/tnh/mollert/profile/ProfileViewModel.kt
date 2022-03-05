package com.tnh.mollert.profile

import android.content.ContentResolver
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.mollert.utils.safeResume
import com.tnh.tnhlibrary.liveData.utils.toLiveData
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.trace
import com.tnh.tnhlibrary.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AppRepository,
    private val firestore: FirestoreHelper,
    private val storage: StorageHelper
): BaseViewModel() {

    private val email = UserWrapper.getInstance()?.currentUserEmail ?: ""
    val member: LiveData<Member?> = repository.memberDao.getByEmailFlow(email).asLiveData()

    val memberName = Transformations.map(member){ m-> m?.name ?: "" }
    val memberAvatar = Transformations.map(member){m-> m?.avatar ?: "" }
    val memberEmail = Transformations.map(member){ m-> m?.email ?: ""}
    val bio = Transformations.map(member){m-> m?.biography ?: ""}

    private val _editAvatar = MutableLiveData("")
    val editAvatar = _editAvatar.toLiveData()

    fun setImageUri(uriString: String){
        _editAvatar.postValue(uriString)
    }

    suspend fun getBoardByEmail(email: String): List<MemberBoardRel>{
        return repository.memberBoardDao.getRelsByEmailId(email)
    }

    fun saveMemberInfoToFirestore(name: String, bio: String, contentResolver: ContentResolver) {
        if(email.isNotEmpty()){
            viewModelScope.launch {
                val avatarUri = editAvatar.value ?: ""
                var avatar = member.value?.avatar ?: ""
                if(avatarUri.isNotEmpty()){
                    try{
                        storage.uploadImage(
                            storage.getAvatarLocation(email),
                            contentResolver,
                            avatarUri.toUri()
                        )?.let {
                            avatar = it.toString()
                        }
                    }catch (e: Exception){
                        trace(e)
                    }
                }
                val remoteMember =  RemoteMember(email, name, avatar, bio).info()

                val doc = firestore.getMemberDoc(email)
                if (firestore.mergeDocument(doc, remoteMember)) {
                    // succeeded
                    notifyInfoChanged(email)
                    postMessage("Edit profile successfully")
                    dispatchClickEvent(EVENT_SUCCESS)
                } else {
                    // failed
                }
            }
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String) = suspendCancellableCoroutine<Boolean> { cont->
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(memberEmail.value!!, oldPassword)
        viewModelScope.launch {
            firebaseUser?.let { user ->
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        firebaseUser.updatePassword(newPassword)
                            .addOnSuccessListener {
                                cont.safeResume { true }
                            }
                            .addOnFailureListener { error ->
                                trace(error)
                                postMessage("Change password failure")
                                cont.safeResume { false }
                            }
                    }
                    .addOnFailureListener { e ->
                        trace(e)
                        postMessage("Old password invalid, please try again")
                        cont.safeResume { false }
                    }
            }
        }
    }

    private suspend fun notifyInfoChanged(email: String) {
        val listMembers: MutableSet<Member> = mutableSetOf()
        repository.appDao.getMemberWithWorkspacesNoFlow(email)?.workspaces?.forEach { workspace->
            repository.appDao.getMemberByWorkspaceId(workspace.workspaceId).forEach {
                listMembers.add(it)
            }
        }

        if(listMembers.isEmpty()){
            firestore.insertToArrayField(firestore.getTrackingDoc(email), "info", email)
        }else{
            listMembers.forEach {
                firestore.insertToArrayField(firestore.getTrackingDoc(it.email), "info", email)
            }
        }
    }

    companion object {
        const val EVENT_LOGOUT_CLICKED = "logout_clicked"
        const val EVENT_PROFILE_IMAGE_CLICKED = "profile_image_clicked"
        const val EVENT_SUCCESS = "success"
        const val CHANGE_PASSWORD_SUCCESS = "change_pass_success"
        const val CHANGE_PASSWORD_FAILURE = "change_pass_failure"
    }
}