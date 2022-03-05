package com.tnh.mollert

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.tnh.mollert.databinding.ActivityMainBinding
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.utils.UserWrapper
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
                hideBottomNav()
                viewModel.unregisterRemoteEvent()
            }
        ) {
            viewModel.registerRemoteEvent()
        }
        setContentView(binding.root)
    }

    private fun setupBottomNav(){
        binding.activityMainBottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.cardDetailFragment, R.id.splashFragment, R.id.loginFragment, R.id.registerFragment, R.id.forgotPasswordFragment,
                    R.id.addEditLabelFragment,
                    R.id.dashboardFragment
                ->{
                    hideBottomNav()
                }
                else->{
                    showBottomNav()
                }
            }

        }
    }


    fun showBottomNav(){
        binding.activityMainBottomNav.show()
    }

    fun hideBottomNav(){
        binding.activityMainBottomNav.gone()
    }

}