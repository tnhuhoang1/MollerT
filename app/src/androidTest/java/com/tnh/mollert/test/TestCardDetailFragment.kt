package com.tnh.mollert.test

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseAuth
import com.tnh.mollert.ActivityTestWithDataBindingIdlingResources
import com.tnh.mollert.MainCoroutineRule
import com.tnh.mollert.R
import com.tnh.mollert.boardDetail.BoardDetailFragmentArgs
import com.tnh.mollert.cardDetail.CardDetailFragmentArgs
import com.tnh.mollert.datasource.DataSource
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberCardRel
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import com.tnh.mollert.utils.LabelPreset
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
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

    val workspace = Workspace("workspace_id", "My workspace")
    val mw = MemberWorkspaceRel(member.email, workspace.workspaceId)
    val board = Board("board_id", "My board", workspace.workspaceId)
    val mb = MemberBoardRel(member.email, board.boardId, MemberBoardRel.ROLE_OWNER)
    val list1 = List("list_id_1", "list1", board.boardId, List.STATUS_ACTIVE, 0)
    val list2 = List("list_id_2", "list2", board.boardId, List.STATUS_ACTIVE, 0)
    val list3 = List("list_id_3", "list3", board.boardId, List.STATUS_ACTIVE, 0)
    val list4 = List("list_id_4", "list4", board.boardId, List.STATUS_ACTIVE, 0)
    val list5 = List("list_id_5", "list5", board.boardId, List.STATUS_ACTIVE, 0)
    val card11 = Card("card_1_1", "card_1_1", 0, list1.listId)
    val card12 = Card("card_1_2", "card_1_2", 0, list1.listId)
    val card13 = Card("card_1_3", "card_1_3", 0, list1.listId)
    val card14 = Card("card_1_4", "card_1_4", 0, list1.listId)
    val card15 = Card("card_1_5", "card_1_5", 0, list1.listId)
    val card21 = Card("card_2_1", "card_2_1", 0, list2.listId)
    val card22 = Card("card_2_2", "card_2_2", 0, list2.listId)
    val card23 = Card("card_2_3", "card_2_3", 0, list2.listId)
    val labels = LabelPreset.getPresetLabelList(board.boardId).map { it.toLabel()!! }
    val cardLabelRel1 = CardLabelRel(card11.cardId, labels[0].labelId)
    val cardLabelRel2 = CardLabelRel(card11.cardId, labels[1].labelId)
    val cardLabelRel3 = CardLabelRel(card11.cardId, labels[2].labelId)
    val memberCardRel1 = MemberCardRel(member.email, card11.cardId, MemberBoardRel.ROLE_MEMBER)
    val memberCardRel2 = MemberCardRel(member.email, card12.cardId, MemberBoardRel.ROLE_MEMBER)
    val memberCardRel3 = MemberCardRel(member.email, card13.cardId, MemberBoardRel.ROLE_MEMBER)
    val memberCardRel4 = MemberCardRel(member.email, card14.cardId, MemberBoardRel.ROLE_MEMBER)
    private fun setupData() = mainCoroutine.runBlockingTest{
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.memberWorkspaceDao.insertOne(mw)
        dataSource.memberBoardDao.insertOne(mb)
        dataSource.listDao.insertAll(list1, list2, list3, list4, list5)
        dataSource.cardDao.insertAll(card11, card12, card13, card14, card15, card21, card22, card23)
        dataSource.labelDao.insertAll(*labels.toTypedArray())
    }

    @Test
    fun runTest(){
        setupData()
        //test
        val cardArgs = CardDetailFragmentArgs.Builder(workspace.workspaceId, board.boardId, list1.listId, card11.cardId).build().toBundle()
        join_card(cardArgs)
        leave_card(cardArgs)
        add_date_to_card()
        add_labels(cardArgs)
        mark_card_done()
    }



    private fun join_card(args: Bundle) = mainCoroutine.runBlockingTest{
        launchTestFragmentWithContainer(R.id.cardDetailFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Join card")).perform(click())
            dataSource.memberCarDao.insertOne(memberCardRel1)
            onView(withId(R.id.member_avatar_item_avatar)).check(matches(isDisplayed()))
        }
    }

    private fun leave_card(args: Bundle) = mainCoroutine.runBlockingTest{
        // test manual
        launchTestFragmentWithContainer(R.id.cardDetailFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Leave card")).perform(click())
            dataSource.memberCarDao.deleteOne(memberCardRel1)
            onView(withId(R.id.member_avatar_item_avatar)).check(matches(not(isDisplayed())))
        }
    }

    private fun add_date_to_card() = mainCoroutine.runBlockingTest{
        // manual test
    }


    private fun mark_card_done() = mainCoroutine.runBlockingTest {
        // manual test
    }

    private fun add_labels(args: Bundle) = mainCoroutine.runBlockingTest {
        launchTestFragmentWithContainer(R.id.cardDetailFragment, args){
            onView(withId((R.id.card_detail_fragment_label))).perform(click())
            onView(withText(labels[0].labelName)).perform(click())
            onView(withText(labels[1].labelName)).perform(click())
            onView(withText(labels[2].labelName)).perform(click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            dataSource.cardLabelDao.insertAll(cardLabelRel1, cardLabelRel2, cardLabelRel3)
//            sleep(2000)
//            onView(withText("Set labels successfully")).check(matches(isDisplayed()))
        }
    }

}