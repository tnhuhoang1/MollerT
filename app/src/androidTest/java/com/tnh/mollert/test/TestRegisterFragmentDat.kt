package com.tnh.mollert.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.ActivityTestWithDataBindingIdlingResources
import com.tnh.mollert.MainCoroutineRule
import com.tnh.mollert.R
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
class TestRegisterFragmentDat : ActivityTestWithDataBindingIdlingResources() {
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
    fun runTest(){
        register_with_no_input()
        register_with_missing_input()
        register_with_invalid_password()
        register_with_invalid_email()
        register_with_two_password_not_equal()
        register_with_email_taken()
        register_with_success()
    }

    fun register_with_no_input() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.registerFragment){
            onView(withId(R.id.register_fragment_sign_up_button)).perform(scrollTo(), click())
            onView(withText("Please fill out the form to continue")).check(matches(isDisplayed()))
        }
    }

    fun register_with_missing_input() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.registerFragment){
            onView(withId(R.id.register_fragment_email)).perform(scrollTo(), typeText("dat@1.1"))
            onView(withId(R.id.register_fragment_sign_up_button)).perform(scrollTo(), click())
            onView(withText("Please fill out the form to continue")).check(matches(isDisplayed()))
            onView(withId(R.id.register_fragment_password)).perform(scrollTo(), typeText("1234567"))
            onView(withId(R.id.register_fragment_sign_up_button)).perform(scrollTo(), click())
            onView(withText("Please fill out the form to continue")).check(matches(isDisplayed()))
        }
    }

    fun register_with_invalid_password() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.registerFragment){
            onView(withId(R.id.register_fragment_email)).perform(scrollTo(), typeText("dat@1.1"))
            onView(withId(R.id.register_fragment_password)).perform(scrollTo(), typeText("123456"))
            onView(withId(R.id.register_fragment_confirm_password)).perform(scrollTo(), typeText("123456"))
            onView(withId(R.id.register_fragment_sign_up_button)).perform(scrollTo(), click())
            onView(withText("Password invalid, please try again")).check(matches(isDisplayed()))
        }
    }

    fun register_with_invalid_email() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.registerFragment){
            onView(withId(R.id.register_fragment_email)).perform(scrollTo(), typeText("just some text"))
            onView(withId(R.id.register_fragment_password)).perform(scrollTo(), typeText("1234567"))
            onView(withId(R.id.register_fragment_confirm_password)).perform(scrollTo(), typeText("1234567"))
            onView(withId(R.id.register_fragment_sign_up_button)).perform(scrollTo(), click())
            onView(withText("Email invalid, please try again")).check(matches(isDisplayed()))
        }
    }

    fun register_with_two_password_not_equal() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.registerFragment){
            onView(withId(R.id.register_fragment_email)).perform(scrollTo(), typeText("dat@1.1"))
            onView(withId(R.id.register_fragment_password)).perform(scrollTo(), typeText("1234567"))
            onView(withId(R.id.register_fragment_confirm_password)).perform(scrollTo(), typeText("1111111"))
            onView(withId(R.id.register_fragment_sign_up_button)).perform(scrollTo(), click())
            onView(withText("Password and confirm password must be equal")).check(matches(isDisplayed()))
        }
    }

    fun register_with_email_taken() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.registerFragment){
            onView(withId(R.id.register_fragment_email)).perform(scrollTo(), typeText("dat@1.1"))
            onView(withId(R.id.register_fragment_password)).perform(scrollTo(), typeText("1111111"))
            onView(withId(R.id.register_fragment_confirm_password)).perform(scrollTo(), typeText("1111111"))
            onView(withId(R.id.register_fragment_sign_up_button)).perform(scrollTo(), click())
            sleep(1000)
            onView(withText("The email address is already taken")).check(matches(isDisplayed()))
        }
    }

    fun register_with_success() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.registerFragment){
            //manual test
//            onView(withId(R.id.register_fragment_email)).perform(scrollTo(), typeText("dattest@1.1"))
//            onView(withId(R.id.register_fragment_password)).perform(scrollTo(), typeText("1111111"))
//            onView(withId(R.id.register_fragment_confirm_password)).perform(scrollTo(), typeText("1111111"))
//            onView(withId(R.id.register_fragment_sign_up_button)).perform(scrollTo(), click())
//            sleep(1000)
//            onView(withText("Welcome!")).check(matches(isDisplayed()))
        }
    }


}