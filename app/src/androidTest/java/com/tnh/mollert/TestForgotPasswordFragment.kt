package com.tnh.mollert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
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
class TestForgotPasswordFragment : ActivityTestWithDataBindingIdlingResources() {
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
    fun test_all() {
        forgot_password_with_no_input()
        forgot_password_with_invalid_email()
        forgot_password_with_email_not_exists()
        forgot_password_success()
    }

    private fun forgot_password_with_no_input() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.forgotPasswordFragment){
            onView(withId(R.id.forgot_password_fragment_sign_in)).perform(click())
            onView(withText("Email invalid, please try again")).check(matches(isDisplayed()))
            onView(withId(R.id.forgot_password_fragment_email)).perform(typeText("just some text"))
            closeSoftKeyboard()
            onView(withId(R.id.forgot_password_fragment_sign_in)).perform(click())
            onView(withText("Email invalid, please try again")).check(matches(isDisplayed()))
        }
    }

    private fun forgot_password_with_invalid_email() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.forgotPasswordFragment){
            onView(withId(R.id.forgot_password_fragment_sign_in)).perform(click())
            onView(withText("Email invalid, please try again")).check(matches(isDisplayed()))
            onView(withId(R.id.forgot_password_fragment_email)).perform(typeText("just some text"))
            closeSoftKeyboard()
            onView(withId(R.id.forgot_password_fragment_sign_in)).perform(click())
            onView(withText("Email invalid, please try again")).check(matches(isDisplayed()))
        }
    }

    private fun forgot_password_with_email_not_exists() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.forgotPasswordFragment){
            onView(withId(R.id.forgot_password_fragment_email)).perform(typeText("datDay@1.1"))
            closeSoftKeyboard()
            onView(withId(R.id.forgot_password_fragment_sign_in)).perform(click())
            onView(withText("This email isn’t linked to any account")).check(matches(isDisplayed()))
        }
    }

    private fun forgot_password_success() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.forgotPasswordFragment){
            onView(withId(R.id.forgot_password_fragment_email)).perform(typeText("dat@1.1"))
            closeSoftKeyboard()
            onView(withId(R.id.forgot_password_fragment_sign_in)).perform(click())
            sleep(1000)
            onView(withId(R.id.login_fragment_title)).check(matches(isDisplayed()))
            onView(withText("Reset password successfully, please check your email")).check(matches(isDisplayed()))
        }
    }
}