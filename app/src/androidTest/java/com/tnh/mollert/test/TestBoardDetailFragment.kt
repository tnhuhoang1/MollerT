package com.tnh.mollert.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
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
import com.tnh.mollert.boardDetail.BoardDetailFragmentArgs
import com.tnh.mollert.clickItemWithIdAndChildText
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
import kotlinx.coroutines.delay
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
class TestBoardDetailFragment : ActivityTestWithDataBindingIdlingResources() {
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

    @Test
    fun runTest(){
        setupData()
        //test
        add_new_board_no_name()
        add_new_board_no_visibility()
        add_new_board_correct()
        change_board_background()
        change_board_desc()
        find_board_no_result()
        find_board_with_result()
        close_board()
        reopen_board()
        show_dash_board()
        show_activities()
    }

    private fun setupData() = mainCoroutine.runBlockingTest{
        dataSource.workspaceDao.insertOne(workspace)
        dataSource.memberWorkspaceDao.insertOne(mw)
    }

    private fun add_new_board_no_name() = mainCoroutine.runBlockingTest{
        // test manual
    }

    private fun add_new_board_no_visibility() = mainCoroutine.runBlockingTest{
        // test manual
    }

    private fun add_new_board_correct() = mainCoroutine.runBlockingTest{
        dataSource.memberDao.insertOne(member)
        launchTestFragmentWithContainer(R.id.homeFragment){
            onView(withId(R.id.workspace_item_new)).perform(click())
            onView(withId(R.id.create_board_dialog_name)).perform(typeText(board.boardName))
            Espresso.closeSoftKeyboard()
            onView(withId(R.id.create_board_dialog_public)).perform(click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            dataSource.boardDao.insertOne(board)
            sleep(100)
            onView(withText("Board added")).check(matches(isDisplayed()))
            onView(withText(board.boardName)).check(matches(isDisplayed()))
        }
    }


    private fun change_board_background() = mainCoroutine.runBlockingTest {
        dataSource.boardDao.insertOne(board)
        val args = BoardDetailFragmentArgs.Builder(workspace.workspaceId, board.boardId, board.boardName).build().toBundle()
        launchTestFragmentWithContainer(R.id.boardDetailFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Change background")).perform(click())
            onView(withId(R.id.create_board_dialog_recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    3,
                    click()
                )
            )
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            val boardBackground = "https://img4.thuthuatphanmem.vn/uploads/2020/12/26/background-gradient-tim_052413985.jpg"
            dataSource.boardDao.insertOne(board.copy(background = boardBackground))
            sleep(1000)
            onView(withText("Change background successfully")).check(matches(isDisplayed()))
        }
    }

    private fun change_board_desc() = mainCoroutine.runBlockingTest {
        dataSource.boardDao.insertOne(board)
        val args = BoardDetailFragmentArgs.Builder(workspace.workspaceId, board.boardId, board.boardName).build().toBundle()
        launchTestFragmentWithContainer(R.id.boardDetailFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            val boardDesc = "Welcome to this board!!!"
            onView(withText("Board description")).perform(click())
            onView(withId(R.id.board_desc_desc)).perform(typeText(boardDesc))
            Espresso.closeSoftKeyboard()
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            dataSource.boardDao.insertOne(board.copy(boardDesc = boardDesc))
            sleep(100)
            onView(withText("Change description successfully")).check(matches(isDisplayed()))
        }
    }


    private fun find_board_no_result() = mainCoroutine.runBlockingTest{
        dataSource.boardDao.insertOne(board)
        launchTestFragmentWithContainer(R.id.homeFragment){
            onView(withId(R.id.home_fragment_search_input)).perform(typeText("No result"))
            onView(withContentDescription("search icon")).perform(click())
            onView(withText("Found nothing")).check(matches(isDisplayed()))
        }
    }

    private fun find_board_with_result() = mainCoroutine.runBlockingTest{
        dataSource.boardDao.insertOne(board)
        launchTestFragmentWithContainer(R.id.homeFragment){
            onView(withId(R.id.home_fragment_search_input)).perform(typeText(board.boardName))
            onView(withContentDescription("search icon")).perform(click())
            onView(withText(board.boardName)).check(matches(isDisplayed()))
        }
    }


    private fun close_board() = mainCoroutine.runBlockingTest {
        dataSource.boardDao.insertOne(board)
        val memberBoardRel = MemberBoardRel(member.email, board.boardId, MemberBoardRel.ROLE_OWNER)
        dataSource.memberBoardDao.insertOne(memberBoardRel)

        launchTestFragmentWithContainer(R.id.homeFragment){
            onView(allOf(withText(board.boardName))).perform(click())
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Close board")).perform(click())
            dataSource.boardDao.insertOne(board.copy(boardStatus = Board.STATUS_CLOSED))
            sleep(200)
            onView(withText("The board was closed")).check(matches(isDisplayed()))
        }
    }

    private fun reopen_board() = mainCoroutine.runBlockingTest {
        dataSource.boardDao.insertOne(board.copy(boardStatus = Board.STATUS_CLOSED))
        launchTestFragmentWithContainer(R.id.homeFragment){
            onView(allOf(withText(board.boardName))).perform(click())
            onView(withText("REOPEN")).perform(click())
            dataSource.boardDao.insertOne(board.copy(boardStatus = Board.STATUS_OPEN))
            sleep(200)
            onView(withText("Reopen board successfully")).check(matches(isDisplayed()))
        }
    }

    private fun show_dash_board() = mainCoroutine.runBlockingTest{
        val list1 = List("list_id_1", "list1", board.boardId, List.STATUS_ACTIVE, 0)
        val list2 = List("list_id_2", "list2", board.boardId, List.STATUS_ACTIVE, 0)
        val list3 = List("list_id_3", "list3", board.boardId, List.STATUS_ACTIVE, 0)
        val list4 = List("list_id_4", "list4", board.boardId, List.STATUS_ACTIVE, 0)
        val list5 = List("list_id_5", "list5", board.boardId, List.STATUS_ACTIVE, 0)
        dataSource.listDao.insertAll(list1, list2, list3, list4, list5)
        val card11 = Card("card_1_1", "card_1_1", 0, list1.listId)
        val card12 = Card("card_1_2", "card_1_2", 0, list1.listId)
        val card13 = Card("card_1_3", "card_1_3", 0, list1.listId)
        val card14 = Card("card_1_4", "card_1_4", 0, list1.listId)
        val card15 = Card("card_1_5", "card_1_5", 0, list1.listId)
        val card21 = Card("card_2_1", "card_2_1", 0, list2.listId)
        val card22 = Card("card_2_2", "card_2_2", 0, list2.listId)
        val card23 = Card("card_2_3", "card_2_3", 0, list2.listId)
        dataSource.cardDao.insertAll(card11, card12, card13, card14, card15, card21, card22, card23)
        val labels = LabelPreset.getPresetLabelList(board.boardId).map { it.toLabel()!! }
        dataSource.labelDao.insertAll(*labels.toTypedArray())
        val cardLabelRel1 = CardLabelRel(card11.cardId, labels[0].labelId)
        val cardLabelRel2 = CardLabelRel(card11.cardId, labels[1].labelId)
        val cardLabelRel3 = CardLabelRel(card11.cardId, labels[2].labelId)
        dataSource.cardLabelDao.insertAll(cardLabelRel1, cardLabelRel2, cardLabelRel3)
        val memberCardRel1 = MemberCardRel(member.email, card11.cardId, MemberBoardRel.ROLE_MEMBER)
        val memberCardRel2 = MemberCardRel(member.email, card12.cardId, MemberBoardRel.ROLE_MEMBER)
        val memberCardRel3 = MemberCardRel(member.email, card13.cardId, MemberBoardRel.ROLE_MEMBER)
        val memberCardRel4 = MemberCardRel(member.email, card14.cardId, MemberBoardRel.ROLE_MEMBER)
        dataSource.memberCarDao.insertAll(memberCardRel1, memberCardRel2, memberCardRel3, memberCardRel4)
        val args = BoardDetailFragmentArgs.Builder(workspace.workspaceId, board.boardId, board.boardName).build().toBundle()

        launchTestFragmentWithContainer(R.id.boardDetailFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Dashboard")).perform(click())
            onView(withText("Cards per list")).check(matches(isDisplayed()))
            sleep(200)

            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Cards per due date")).perform(click())
            onView(withText("Cards per due date")).check(matches(isDisplayed()))
            sleep(200)

            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Cards per member")).perform(click())
            onView(withText("Cards per member")).check(matches(isDisplayed()))
            sleep(200)

            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Cards per label")).perform(click())
            onView(withText("Cards per label")).check(matches(isDisplayed()))
            sleep(200)
        }
    }

    fun show_activities() = mainCoroutine.runBlockingTest{
        val message = MessageMaker.getChangeBoardBackgroundMessage(board.boardId, board.boardName)
        val activity1 = Activity("activity_id1", member.email, board.boardId, null, message, activityType = Activity.TYPE_INFO)
        val activity2 = Activity("activity_id2", member.email, board.boardId, null, message, activityType = Activity.TYPE_INFO)
        val activity3 = Activity("activity_id3", member.email, board.boardId, null, message, activityType = Activity.TYPE_INFO)
        val activity4 = Activity("activity_id4", member.email, board.boardId, null, message, activityType = Activity.TYPE_INFO)
        val activity5 = Activity("activity_id5", member.email, board.boardId, null, message, activityType = Activity.TYPE_INFO)
        val activity6 = Activity("activity_id6", member.email, board.boardId, null, message, activityType = Activity.TYPE_INFO)
        dataSource.activityDao.insertAll(activity1, activity2, activity3, activity4, activity5, activity6)

        val args = BoardDetailFragmentArgs.Builder(workspace.workspaceId, board.boardId, board.boardName).build().toBundle()

        launchTestFragmentWithContainer(R.id.boardDetailFragment, args){
            onView(withId(R.id.two_action_toolbar_end_icon)).perform(click())
            onView(withText("Activities")).perform(click())
            sleep(200)
            onView(withText("Board activities")).check(matches(isDisplayed()))
        }
    }
}