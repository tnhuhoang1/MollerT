package com.tnh.mollert.datasource

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tnh.mollert.datasource.local.dao.AppDao


//@Database(entities = [], version = 1)
abstract class AppRepository: RoomDatabase() {
    abstract val appDao: AppDao

    companion object{
        @Volatile
        private lateinit var instance: AppRepository

        /**
         * get room database
         */
        fun getInstance(context: Context): AppRepository{
            if(::instance.isInitialized.not()){
                instance = Room.databaseBuilder(context, AppRepository::class.java, "database").build()
            }
            return instance
        }
    }
}