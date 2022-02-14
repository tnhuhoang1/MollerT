package com.tnh.mollert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.datasource.remote.model.toMember
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.tnhlibrary.logAny
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.activity_main_fragment_container) as NavHostFragment).navController
    }
    @Inject
    lateinit var repository: AppRepository

    /**
     * don't rely on this property when app start up
     */
    private var currentLocalUser: Member? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listenForUser()
        setContentView(R.layout.activity_main)
    }

    private fun listenForUser(){
        FirebaseAuth.getInstance().addAuthStateListener { state->
            state.currentUser?.let { firebaseUser->
                lifecycleScope.launchWhenCreated {
                    currentLocalUser = repository.memberDao.getByEmail(firebaseUser.email ?: "")
                    currentLocalUser?.let {
                        currentLocalUser = fetchMember(firebaseUser.email ?: "")
                    }
                    currentLocalUser.logAny()
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

}