package com.tnh.mollert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.DataSource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class BaseTestClass : ActivityTestWithDataBindingIdlingResources() {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var instantTask = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    var mainCoroutine = MainCoroutineRule()
    private lateinit var dataSource: DataSource

    override fun setUp() {
        hiltRule.inject()
        super.setUp()
        FirebaseAuth.getInstance().signOut()
        runBlocking {
            coroutineScope {
                dataSource = DataSource.enableTest(ApplicationProvider.getApplicationContext())
            }
        }
    }

    override fun tearDown() {
        super.tearDown()
        dataSource.close()
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun login_no_input() = mainCoroutine.runBlockingTest{
        // test login fragment
        launchTestFragmentWithContainer(R.id.loginFragment){

        }
    }

}