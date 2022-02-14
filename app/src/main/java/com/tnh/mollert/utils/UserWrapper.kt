package com.tnh.mollert.utils

import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.datasource.remote.model.toMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class UserWrapper private constructor(
    private val repository: AppRepository
) {
    val currentUserEmail: String?
        get() {
            return firebaseAuth.currentUser?.email ?: kotlin.run {
                currentLocalUser?.email
            }
        }

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var currentLocalUser: Member? = null

    suspend fun getCurrentUser(): Member?{
        if(firebaseAuth.currentUser != null){
            if(currentLocalUser != null){
                return currentLocalUser
            }
            firebaseAuth.currentUser?.email?.let { email->
                val deferred = coroutineScope.async {
                    currentLocalUser = repository.memberDao.getByEmail(email)
                    currentLocalUser?.let {
                        currentLocalUser = fetchMember(email)
                    }
                    currentLocalUser
                }
                return deferred.await()
            }
        }
        return null
    }

    init {
        listenForUser()
    }

    private fun listenForUser(){
        firebaseAuth.addAuthStateListener { state->
            state.currentUser?.let { firebaseUser->
                coroutineScope.launch {
                    currentLocalUser = repository.memberDao.getByEmail(firebaseUser.email ?: "")
                    currentLocalUser?.let {
                        currentLocalUser = fetchMember(firebaseUser.email ?: "")
                    }
                }
            } ?: kotlin.run {
                currentLocalUser = null
            }
        }
    }

    private suspend fun fetchMember(email: String): Member?{
        FirestoreHelper.getInstance().apply {
            simpleGetDocumentModel<RemoteMember>(
                getMemberDoc(email)
            )?.let { remoteMember->
                val member = remoteMember.toMember()
                return member?.apply {
                    repository.memberDao.insertOne(this)
                }
            }
        }
        return null
    }

    companion object{
        @Volatile
        private lateinit var instance: UserWrapper

        fun getInstance(repository: AppRepository): UserWrapper{
            if(::instance.isInitialized.not()){
                instance = UserWrapper(repository)
            }
            return instance
        }

    }
}