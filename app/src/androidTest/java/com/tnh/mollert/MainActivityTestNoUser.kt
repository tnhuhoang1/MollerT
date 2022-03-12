package com.tnh.mollert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.core.internal.deps.dagger.internal.Preconditions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.boardDetail.BoardDetailFragmentArgs
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.Board
import com.tnh.mollert.datasource.local.model.Workspace
import com.tnh.mollert.home.HomeFragment
import com.tnh.mollert.login.LoginFragment
import com.tnh.mollert.utils.DataBindingIdlingResource
import com.tnh.mollert.utils.monitorActivity
import com.tnh.tnhlibrary.logAny
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.coroutines.resume

@ExperimentalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityTestNoUser : ActivityTestWithDataBindingIdlingResources() {
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


    @Test
    fun login_no_input() = mainCoroutine.runBlockingTest{
        val workspace = Workspace("workspace_id", "workspace")
        val board = Board("board_id", "Hoang", "workspace_id")
        val args = BoardDetailFragmentArgs.Builder(workspace.workspaceId, board.boardId, board.boardName)

        launchTestFragmentWithContainer(R.id.boardDetailFragment, args.build().toBundle()){
//            onView(withId(R.id.login_fragment_sign_in)).perform(click())
//            onView(withText("Please fill out the form")).check(matches(isDisplayed()))
        }
    }

}