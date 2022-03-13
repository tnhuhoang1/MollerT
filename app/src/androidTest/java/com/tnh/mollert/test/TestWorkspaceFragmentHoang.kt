package com.tnh.mollert.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.ActivityTestWithDataBindingIdlingResources
import com.tnh.mollert.MainCoroutineRule
import com.tnh.mollert.R
import com.tnh.mollert.clickItemWithId
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.home.manage.ManageWorkspaceFragmentArgs
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestWorkspaceFragmentHoang : ActivityTestWithDataBindingIdlingResources() {
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
        add_workspace_no_input()
        add_workspace_no_name()
        add_workspace_no_type()
        add_workspace_correct()
        view_workspace_member()
        add_member_to_workspace_your_email()
        add_member_to_workspace_no_email()
        add_member_to_workspace_invalid_email()
        add_member_to_workspace_your_email()
        add_member_to_workspace_no_account_exist()
        add_member_to_workspace_correct()
        join_workspace_already_in()
        join_workspace_not_in()
    }

    val workspace = Workspace("workspace_id", "Workspace Name")
    val workspaceRel = MemberWorkspaceRel(member.email, workspace.workspaceId)

    private fun add_workspace_no_input() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.addWorkspaceFragment){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Please fill out the required fields")).check(matches(isDisplayed()))
        }
    }

    private fun add_workspace_no_name() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.addWorkspaceFragment){
            onView(withId(R.id.add_workspace_fragment_type_container)).perform(click())
            onView(withText("Education")).perform(scrollTo(), click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(scrollTo(), click())
            onView(withText("Please fill out the required fields")).check(matches(isDisplayed()))
        }
    }

    private fun add_workspace_no_type() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.addWorkspaceFragment){
            onView(withId(R.id.add_workspace_fragment_name)).perform(scrollTo(), typeText("New workspace"))
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Please fill out the required fields")).check(matches(isDisplayed()))
        }
    }

    private fun add_workspace_correct() = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.homeFragment){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withId(R.id.add_workspace_fragment_name)).perform(scrollTo(), typeText("New workspace"))
            onView(withId(R.id.add_workspace_fragment_type_container)).perform(click())
            onView(withText("Education")).perform(scrollTo(), click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(scrollTo(), click())
            onView(withText("Create workspace successfully")).check(matches(isDisplayed()))
            onView(withText("New workspace")).check(matches(isDisplayed()))
        }
    }

    private fun view_workspace_member() = mainCoroutine.runBlockingTest{
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.memberWorkspaceDao.insertOne(workspaceRel)
        launchTestFragmentWithContainer(R.id.homeFragment){
            onView(withId(R.id.home_fragment_recycle_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    clickItemWithId(R.id.workspace_item_setting)
                )
            )
            onView(withText(member.name)).check(matches(isDisplayed()))
            sleep(100)
        }
    }

    private fun add_member_to_workspace_no_email() = mainCoroutine.runBlockingTest{
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.memberWorkspaceDao.insertOne(workspaceRel)
        val args = ManageWorkspaceFragmentArgs.Builder(workspace.workspaceId).build().toBundle()
        launchTestFragmentWithContainer(R.id.manageWorkspaceFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("INVITE")).perform(click())
            sleep(100)
            onView(withText("Email address can't be empty")).check(matches(isDisplayed()))
        }
    }

    private fun add_member_to_workspace_invalid_email() = mainCoroutine.runBlockingTest{
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.memberWorkspaceDao.insertOne(workspaceRel)
        val args = ManageWorkspaceFragmentArgs.Builder(workspace.workspaceId).build().toBundle()
        launchTestFragmentWithContainer(R.id.manageWorkspaceFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withId(R.id.create_board_layout_name)).perform(typeText("123214"))
            onView(withText("INVITE")).perform(click())
            sleep(100)
            onView(withText("Invalid email address")).check(matches(isDisplayed()))
        }
    }

    private fun add_member_to_workspace_your_email() = mainCoroutine.runBlockingTest{
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.memberWorkspaceDao.insertOne(workspaceRel)
        val args = ManageWorkspaceFragmentArgs.Builder(workspace.workspaceId).build().toBundle()
        launchTestFragmentWithContainer(R.id.manageWorkspaceFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withId(R.id.create_board_layout_name)).perform(typeText(member.email))
            onView(withText("INVITE")).perform(click())
            sleep(100)
            onView(withText("You can't invite yourself")).check(matches(isDisplayed()))
        }
    }

    fun add_member_to_workspace_no_account_exist() = mainCoroutine.runBlockingTest{
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.memberWorkspaceDao.insertOne(workspaceRel)
        val args = ManageWorkspaceFragmentArgs.Builder(workspace.workspaceId).build().toBundle()
        launchTestFragmentWithContainer(R.id.manageWorkspaceFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withId(R.id.create_board_layout_name)).perform(typeText("never@never.never"))
            onView(withText("INVITE")).perform(click())
            onView(withText("No such member exist")).check(matches(isDisplayed()))
        }
    }

    fun add_member_to_workspace_correct() = mainCoroutine.runBlockingTest{
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.memberWorkspaceDao.insertOne(workspaceRel)
        val args = ManageWorkspaceFragmentArgs.Builder(workspace.workspaceId).build().toBundle()
        launchTestFragmentWithContainer(R.id.manageWorkspaceFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withId(R.id.create_board_layout_name)).perform(typeText("2@2.2"))
            onView(withText("INVITE")).perform(click())
            sleep(1000)
            onView(withText("Sent invitation successfully")).check(matches(isDisplayed()))
        }
    }

    fun join_workspace_already_in() = mainCoroutine.runBlockingTest{
        // test manual
    }


    fun join_workspace_not_in() = mainCoroutine.runBlockingTest{
        // test manual
    }




}

