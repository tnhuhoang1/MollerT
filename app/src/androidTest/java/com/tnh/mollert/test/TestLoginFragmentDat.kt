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
class TestLoginFragmentDat : ActivityTestWithDataBindingIdlingResources() {
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
        login_with_no_input()
        login_with_invalid_email()
        login_with_invalid_password()
        login_with_account_not_exist()
        login_with_incorrect_password()
        login_with_correct_member()
    }


    private fun login_with_no_input() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.loginFragment){
            onView(withId(R.id.login_fragment_sign_in)).perform(scrollTo(), click())
            onView(withText("Please fill out the form to continue")).check(matches(isDisplayed()))
        }
    }

    fun login_with_invalid_email() = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.loginFragment) {
            onView(withId(R.id.login_fragment_email)).perform(scrollTo(), typeText("just some text"))
            onView(withId(R.id.login_fragment_password)).perform(scrollTo(), typeText("1234567"))
            onView(withId(R.id.login_fragment_sign_in)).perform(scrollTo(), click())
            onView(withText("Email invalid, please try again")).check(matches(isDisplayed()))
        }
    }

    fun login_with_invalid_password() = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.loginFragment) {
            onView(withId(R.id.login_fragment_email)).perform(scrollTo(), typeText("dat@1.1"))
            onView(withId(R.id.login_fragment_password)).perform(scrollTo(), typeText("123456"))
            onView(withId(R.id.login_fragment_sign_in)).perform(scrollTo(), click())
            onView(withText("Password invalid, please try again")).check(matches(isDisplayed()))
        }
    }

    fun login_with_account_not_exist() = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.loginFragment) {
            onView(withId(R.id.login_fragment_email)).perform(scrollTo(), typeText("test@1.1"))
            onView(withId(R.id.login_fragment_password)).perform(scrollTo(), typeText("1234567"))
            onView(withId(R.id.login_fragment_sign_in)).perform(scrollTo(), click())
            onView(withText("This account is not exists")).check(matches(isDisplayed()))
        }
    }
    fun login_with_incorrect_password() = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.loginFragment) {
            onView(withId(R.id.login_fragment_email)).perform(scrollTo(), typeText("h@1.1"))
            onView(withId(R.id.login_fragment_password)).perform(scrollTo(), typeText("1111111"))
            onView(withId(R.id.login_fragment_sign_in)).perform(scrollTo(), click())
            sleep(1000)
            onView(withText("Incorrect password")).check(matches(isDisplayed()))
        }
    }
    fun login_with_correct_member() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.loginFragment){
            dataSource.memberDao.insertOne(Member("h@1.1", "Hoang", "", ""))
            onView(withId(R.id.login_fragment_email)).perform(scrollTo(), typeText("h@1.1"))
            onView(withId(R.id.login_fragment_password)).perform(scrollTo(), typeText("1234567"))
            onView(withId(R.id.login_fragment_sign_in)).perform(scrollTo(), click())
            sleep(1000)
            onView(withId(R.id.home_fragment_search_box)).check(matches(isDisplayed()))
        }
    }
}