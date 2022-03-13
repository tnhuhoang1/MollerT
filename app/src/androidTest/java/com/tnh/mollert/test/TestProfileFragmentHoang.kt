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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestProfileFragmentHoang : ActivityTestWithDataBindingIdlingResources() {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var instantTask = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    var mainCoroutine = MainCoroutineRule()

    private lateinit var dataSource: DataSource

    private val member = Member("h@1.1", "Test name")
    override fun setUp() {
        super.setUp()
        runBlocking {
            dataSource = DataSource.enableTest(ApplicationProvider.getApplicationContext())
            dataSource.memberDao.insertOne(member)
            loginWithTestAccount()
        }
    }

    override fun tearDown() {
        super.tearDown()
//        dataSource.close()
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun runTest(){
        edit_profile_info_correct()
        view_profile()
        edit_profile_info_no_name()
        edit_profile_no_action()
        edit_password_with_old_password_wrong()
        edit_password_with_only_new_password()
        edit_password_with_password_invalid()
        edit_password_with_only_old_password()
    }

    private fun view_profile() = mainCoroutine.runBlockingTest{
        dataSource.memberDao.insertOne(member)
        launchTestFragmentWithContainer(R.id.profileFragment){
            onView(withText(member.email)).check(matches(isDisplayed()))
            onView(withText(member.name)).check(matches(isDisplayed()))
        }
    }

    private fun edit_profile_no_action() = mainCoroutine.runBlockingTest{
        dataSource.memberDao.insertOne(member)
        launchTestFragmentWithContainer(R.id.profileFragment){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(allOf(withId(R.id.profile_fragment_email), withText(member.email))).check(matches(isDisplayed()))
            onView(withText(member.name)).check(matches(isDisplayed()))
        }
    }



    private fun edit_profile_info_no_name() = mainCoroutine.runBlockingTest {
        dataSource.memberDao.insertOne(member)
        launchTestFragmentWithContainer(R.id.editProfileFragment){
            onView(withId(R.id.edit_profile_fragment_name)).perform(clearText())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Your name can't be empty")).check(matches(isDisplayed()))
        }
    }

    private fun edit_profile_info_correct() = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.profileFragment){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withId(R.id.edit_profile_fragment_name)).perform(scrollTo(), clearText(), typeText("New Name"))
            onView(withId(R.id.edit_profile_fragment_bio)).perform(scrollTo(), clearText(), typeText("This is my bio"))
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(scrollTo(), click())
            dataSource.memberDao.insertOne(member.copy(name = "New Name", biography = "This is my bio"))
            onView(withText("Change profile successfully")).check(matches(isDisplayed()))
            onView(allOf(withId(R.id.profile_fragment_email), withText(member.email))).check(matches(isDisplayed()))
            onView(allOf(withId(R.id.profile_fragment_profile_name), withText("New Name"))).check(matches(isDisplayed()))
        }
    }

    fun edit_password_with_only_old_password() = mainCoroutine.runBlockingTest{
        dataSource.memberDao.insertOne(member)
        launchTestFragmentWithContainer(R.id.editProfileFragment){
            onView(withId(R.id.edit_profile_fragment_old_password)).perform(scrollTo(), clearText(), typeText("1234"))
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(scrollTo(), click())
            onView(withText("You need old password and new password to change password")).check(matches(isDisplayed()))
        }
    }

    fun edit_password_with_only_new_password() = mainCoroutine.runBlockingTest{
        dataSource.memberDao.insertOne(member)
        launchTestFragmentWithContainer(R.id.editProfileFragment){
            onView(withId(R.id.edit_profile_fragment_password)).perform(scrollTo(), clearText(), typeText("1234"))
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(scrollTo(), click())
            onView(withText("You need old password and new password to change password")).check(matches(isDisplayed()))
        }
    }

    private fun edit_password_with_password_invalid() = mainCoroutine.runBlockingTest{
        dataSource.memberDao.insertOne(member)
        launchTestFragmentWithContainer(R.id.editProfileFragment){
            onView(withId(R.id.edit_profile_fragment_old_password)).perform(scrollTo(), clearText(), typeText("1234"))
            onView(withId(R.id.edit_profile_fragment_password)).perform(scrollTo(), clearText(), typeText("1234"))
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(scrollTo(), click())
            onView(withText("Invalid password, please try again")).check(matches(isDisplayed()))
        }
    }

    private fun edit_password_with_old_password_wrong() = mainCoroutine.runBlockingTest {
        dataSource.memberDao.insertOne(member)
        launchTestFragmentWithContainer(R.id.editProfileFragment){
            onView(withId(R.id.edit_profile_fragment_old_password)).perform(scrollTo(), clearText(), typeText("1111111111"))
            onView(withId(R.id.edit_profile_fragment_password)).perform(scrollTo(), clearText(), typeText("123478999"))
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(scrollTo(), click())
            sleep(500)
            onView(withText("Your old password is incorrect")).check(matches(isDisplayed()))
        }
    }

}