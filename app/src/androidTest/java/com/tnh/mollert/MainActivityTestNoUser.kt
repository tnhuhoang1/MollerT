package com.tnh.mollert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.utils.DataBindingIdlingResource
import com.tnh.mollert.utils.monitorActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityTestNoUser : ActivityTestWithDataBindingIdlingResources() {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var instantTask = InstantTaskExecutorRule()

    override fun setUp() {
        super.setUp()
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun splash_to_login_register_and_return_to_splash(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        monitorActivityScenario(activityScenario)

        onView(withId(R.id.splash_fragment_sign_in)).perform(click())
        onView(withId(R.id.login_fragment_sign_up_button)).perform(click())
        onView(withId(R.id.register_fragment_back_button)).perform(click())
        activityScenario.close()
    }

    @Test
    fun splash_to_register_login_and_return_to_splash(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        monitorActivityScenario(activityScenario)

        onView(withId(R.id.splash_fragment_sign_up)).perform(click())
        onView(withId(R.id.register_fragment_sign_in_button)).perform(scrollTo(), click())
        onView(withId(R.id.login_fragment_back_button)).perform(click())
        activityScenario.close()
    }

    @Test
    fun splash_to_login_forgot_register_and_return(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        monitorActivityScenario(activityScenario)
        onView(withId(R.id.splash_fragment_sign_in)).perform(click())
        onView(withId(R.id.login_fragment_forgot_account)).perform(click())
        onView(withId(R.id.forgot_password_fragment_forgot_account)).perform(click())
        onView(withId(R.id.register_fragment_back_button)).perform(click())
        activityScenario.close()
    }






}