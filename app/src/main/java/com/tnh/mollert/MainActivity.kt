package com.tnh.mollert

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.databinding.ActivityMainBinding
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.datasource.local.model.Member
import com.tnh.mollert.datasource.remote.model.RemoteMember
import com.tnh.mollert.datasource.remote.model.toMember
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.UserWrapper
import com.tnh.tnhlibrary.logAny
import com.tnh.tnhlibrary.view.gone
import com.tnh.tnhlibrary.view.show
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<ActivityViewModel>()
    val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.activity_main_fragment_container) as NavHostFragment).navController
    }
    @Inject
    lateinit var repository: AppRepository
    private lateinit var userWrapper: UserWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        userWrapper = UserWrapper.getInstance(repository)
        setupBottomNav()
        userWrapper.listenForUser(
            {
                hidBottomNav()
            }
        ) {
            viewModel.registerRemoteEvent()
        }
        setContentView(binding.root)
    }

    fun setupBottomNav(){
        binding.activityMainBottomNav.setupWithNavController(navController)
    }


    fun showBottomNav(){
        binding.activityMainBottomNav.show()
    }

    fun hidBottomNav(){
        binding.activityMainBottomNav.gone()
    }

}