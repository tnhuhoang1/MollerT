package com.tnh.mollert

import android.view.Menu
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.Member
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
class TestLoginFragment : ActivityTestWithDataBindingIdlingResources() {
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

}