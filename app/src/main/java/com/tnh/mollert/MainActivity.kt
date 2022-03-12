package com.tnh.mollert

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.tnh.mollert.databinding.ActivityMainBinding
import com.tnh.mollert.datasource.DataSource
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
    lateinit var repository: DataSource
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

//        binding.activityMainBottomNav.setupWithNavController(navController)
        binding.activityMainBottomNav.setOnItemSelectedListener { menuItem->
            when(menuItem.itemId){
                R.id.homeFragment->{
                    if(navController.currentDestination?.id != R.id.homeFragment){
                        navController.navigate(R.id.action_global_home)
                    }
                }
                R.id.calendarFragment->{
                    if(navController.currentDestination?.id != R.id.calendarFragment){
                        navController.navigate(R.id.action_global_deadline)
                    }
                }
                R.id.notificationFragment->{
                    if(navController.currentDestination?.id != R.id.notificationFragment){
                        navController.navigate(R.id.action_global_notification)
                    }
                }
                R.id.profileFragment->{
                    if(navController.currentDestination?.id != R.id.profileFragment){
                        navController.navigate(R.id.action_global_profile)
                    }
                }
            }
            true
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.homeFragment->{
                    binding.activityMainBottomNav.menu.children.forEach {
                        if(it.itemId == R.id.homeFragment){
                            it.isChecked = true
                        }
                    }
                }
                R.id.calendarFragment->{
                    binding.activityMainBottomNav.menu.children.forEach {
                        if(it.itemId == R.id.calendarFragment){
                            it.isChecked = true
                        }
                    }
                }
                R.id.notificationFragment->{
                    binding.activityMainBottomNav.menu.children.forEach {
                        if(it.itemId == R.id.notificationFragment){
                            it.isChecked = true
                        }
                    }
                }
                R.id.profileFragment->{
                    binding.activityMainBottomNav.menu.children.forEach {
                        if(it.itemId == R.id.profileFragment){
                            it.isChecked = true
                        }
                    }
                }
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