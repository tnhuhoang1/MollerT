package com.tnh.mollert

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.cardDetail.CardDetailFragmentArgs
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestCardDetailFragment : ActivityTestWithDataBindingIdlingResources() {
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

    private fun initArgs(): Bundle {
        val member = Member("dat@test.com","dat")
        val workspace = Workspace("board_id","board_name")
        val memberWorkSpace = MemberWorkspaceRel(member.email,workspace.workspaceId)
        val board = Board("board_id","board_name",workspace.workspaceId)
        val list = List("list_id","list_name",board.boardId, List.STATUS_ACTIVE,0)
        val card = Card("card_id","card_name",0,list.listId,0L, Card.STATUS_ACTIVE)
        mainCoroutine.runBlockingTest {
            dataSource.memberDao.insertOne(member)
            dataSource.memberWorkspaceDao.insertOne(memberWorkSpace)
            dataSource.workspaceDao.insertOne(workspace)
            dataSource.boardDao.insertOne(board)
            dataSource.listDao.insertOne(list)
            dataSource.cardDao.insertOne(card)
        }
        return CardDetailFragmentArgs.Builder(workspace.workspaceId,board.boardId,list.listId,card.cardId).build().toBundle()
    }

    @Test
    fun test_work_function() {
        val args = initArgs()
        add_work_with_no_name(args)
        add_work_success(args)
        //delete_work(args) error "Animations may only be started on the main thread"
    }

    @Test
    fun test_link_attachment() {
        val args = initArgs()
        add_link_attachment_null(args)
        add_link_attachment_with_wrong_url(args)
        add_link_attachment_success(args)
        delete_link_attachment(args)
    }

    private fun add_work_with_no_name(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.card_detail_fragment_checked_list)).perform(click())
            onView(withText("OK")).perform(click())
            onView(withText("Work name can't be empty")).check(matches(isDisplayed()))
        }
    }

    private fun add_work_success(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.card_detail_fragment_checked_list)).perform(click())
            onView(withHint("Work name")).perform(typeText("test_work"))
            onView(withText("OK")).perform(click())
            sleep(1500)
            onView(withText("Add work successfully")).check(matches(isDisplayed()))
        }
    }

    private fun delete_work(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.workDao.insertOne(Work("work_id","work_name","card_id"))
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.work_item_delete_work)).perform(click())
            onView(withText("DELETE")).perform(click())
            sleep(1500)
            onView(withText("Delete work successfully")).check(matches(isDisplayed()))
        }
    }

    private fun add_link_attachment_null(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.card_detail_fragment_attachment)).perform(click())
            onView(withId(R.id.add_attachment_link)).perform(click())
            onView(withText("OK")).perform(click())
            onView(withText("Link can't be empty")).check(matches(isDisplayed()))
        }
    }

    private fun add_link_attachment_with_wrong_url(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.card_detail_fragment_attachment)).perform(click())
            onView(withId(R.id.add_attachment_link)).perform(click())
            onView(withHint("Link")).perform(typeText("not a link"))
            onView(withText("OK")).perform(click())
            onView(withText("Not an url")).check(matches(isDisplayed()))
        }
    }

    private fun add_link_attachment_success(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.card_detail_fragment_attachment)).perform(click())
            onView(withId(R.id.add_attachment_link)).perform(click())
            onView(withHint("Link")).perform(typeText("dat.test"))
            onView(withText("OK")).perform(click())
            dataSource.attachmentDao.insertOne(Attachment("attachment_id","attachment_name",Attachment.TYPE_LINK,"dat.test","card_id"))
            sleep(1500)
            onView(withText("dat.test")).check(matches(isDisplayed()))
            onView(withText("Add attachment successfully")).check(matches(isDisplayed()))
        }
    }

    private fun delete_link_attachment(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.attachmentDao.insertOne(Attachment("attachment_id","attachment_name",Attachment.TYPE_LINK,"dat.test","card_id"))
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withRecyclerView(R.id.card_detail_fragment_attachment_recycler).atPosition(0)).perform(longClick())
            onView(withText("DELETE")).perform(click())
            sleep(1000)
            onView(withText("Delete attachment successfully")).check(matches(isDisplayed()))
        }
    }
}