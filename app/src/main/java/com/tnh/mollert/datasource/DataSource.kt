package com.tnh.mollert.datasource

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tnh.mollert.datasource.local.dao.*
import com.tnh.mollert.datasource.local.model.*
import com.tnh.mollert.datasource.local.model.List
import com.tnh.mollert.datasource.local.relation.CardLabelRel
import com.tnh.mollert.datasource.local.relation.MemberBoardRel
import com.tnh.mollert.datasource.local.relation.MemberCardRel
import com.tnh.mollert.datasource.local.relation.MemberWorkspaceRel
import kotlinx.coroutines.runBlocking


@Database(
    entities = [
        Attachment::class, Automation::class, Board::class,
        Card::class, Label::class, List::class, Activity::class,
        Member::class, Task::class, Work::class, Workspace::class,
        MemberWorkspaceRel::class, MemberBoardRel::class, MemberCardRel::class,
        CardLabelRel::class
    ], version = 1
)
abstract class DataSource() : RoomDatabase() {
    abstract val appDao: AppDao
    abstract val activityDao: ActivityDao
    abstract val attachmentDao: AttachmentDao
    abstract val automationDao: AutomationDao
    abstract val boardDao: BoardDao
    abstract val cardDao: CardDao
    abstract val labelDao: LabelDao
    abstract val listDao: ListDao
    abstract val memberDao: MemberDao
    abstract val taskDao: TaskDao
    abstract val workDao: WorkDao
    abstract val workspaceDao: WorkspaceDao


    abstract val memberWorkspaceDao: MemberWorkspaceDao
    abstract val memberCarDao: MemberCardDao
    abstract val memberBoardDao: MemberBoardDao
    abstract val cardLabelDao: CardLabelDao

    companion object {
        @Volatile
        private lateinit var instance: DataSource

        /**
         * get room database
         */
        fun getInstance(context: Context): DataSource {
            if (::instance.isInitialized.not()) {
                instance =
                    Room.databaseBuilder(context, DataSource::class.java, "database").build()
            }
            return instance
        }

        fun enableTest(context: Context): DataSource{
            instance = Room.inMemoryDatabaseBuilder(context, DataSource::class.java).allowMainThreadQueries().build()
            return instance
        }

        private val look = Any()

        @VisibleForTesting
        fun resetRepository(){
            synchronized(look){
                instance.apply {
                    clearAllTables()
                    close()
                }
            }
        }
    }
}