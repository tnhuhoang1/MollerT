package com.tnh.mollert.test

import android.os.Bundle
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
import com.tnh.mollert.*
import com.tnh.mollert.R
import com.tnh.mollert.cardDetail.CardDetailFragmentArgs
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
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
class TestCardDetailFragmentDat : ActivityTestWithDataBindingIdlingResources() {
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
                loginWithTestAccount()
            }
        }
    }

    override fun tearDown() {
        super.tearDown()
        dataSource.close()
        FirebaseAuth.getInstance().signOut()
    }

    private val member = Member("dat@test.com","dat")
    private val workspace = Workspace("board_id","board_name")
    private val memberWorkSpace = MemberWorkspaceRel(member.email,workspace.workspaceId)
    private val board = Board("board_id","board_name",workspace.workspaceId)
    private val memberBoardRel = MemberBoardRel(member.email, board.boardId, MemberBoardRel.ROLE_OWNER)
    private val list = List("list_id","list_name",board.boardId, List.STATUS_ACTIVE,0)
    private val card = Card("card_id","card_name",0,list.listId,0L, Card.STATUS_ACTIVE)
    private val comment = Activity("activity_id","h@1.1",board.boardId,card.cardId,MessageMaker.getCommentMessage(card.cardId,card.cardName,"Hello, i'm Dat"),false,Activity.TYPE_COMMENT)
    private fun initArgs(): Bundle {
        mainCoroutine.runBlockingTest {
            dataSource.memberDao.insertOne(member)
            dataSource.memberWorkspaceDao.insertOne(memberWorkSpace)
            dataSource.workspaceDao.insertOne(workspace)
            dataSource.boardDao.insertOne(board)
            dataSource.listDao.insertOne(list)
            dataSource.cardDao.insertOne(card)
            dataSource.memberBoardDao.insertOne(memberBoardRel)
        }
        return CardDetailFragmentArgs.Builder(workspace.workspaceId,board.boardId,list.listId,card.cardId).build().toBundle()
    }

    @Test
    fun runTest(){
        val args = initArgs()
        add_work_with_no_name(args)
        add_work_success(args)
        delete_work(args)
        add_link_attachment_null(args)
        add_link_attachment_with_wrong_url(args)
        add_link_attachment_success(args)
        delete_link_attachment(args)
        add_comment(args)
        delete_comment(args)
    }

    private fun add_work_with_no_name(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.card_detail_fragment_checked_list)).perform(click())
            onView(withText("OK")).perform(click())
            onView(withText("Work name can't be empty")).check(matches(isDisplayed()))
        }
    }

    private fun add_work_success(args: Bundle) = mainCoroutine.runBlockingTest {
        val work = Work("work_id", "Work name", card.cardId)
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withId(R.id.card_detail_fragment_checked_list)).perform(click())
            onView(withHint("Work name")).perform(typeText("test_work"))
            onView(withText("OK")).perform(click())
            dataSource.workDao.insertOne(work)
            onView(withText("Add work successfully")).check(matches(isDisplayed()))
            onView(withText(work.workName)).check(matches(isDisplayed()))
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

    private fun add_comment(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withHint("Comment")).perform(typeText("Hello, i'm Dat"))
            closeSoftKeyboard()
            onView(withId(R.id.card_detail_fragment_send)).perform(click())
            dataSource.activityDao.insertOne(comment)
            onView(withText("Hello, i'm Dat")).check(matches(isDisplayed()))
        }
    }

    private fun delete_comment(args: Bundle) = mainCoroutine.runBlockingTest {
        dataSource.activityDao.insertOne(comment)
        launchTestFragmentWithContainer(R.id.cardDetailFragment,args) {
            onView(withText("Hello, i'm Dat")).perform(longClick())
            onView(withText("DELETE")).perform(longClick())
            onView(withText("Delete comment successfully")).check(matches(isDisplayed()))
        }
    }
}