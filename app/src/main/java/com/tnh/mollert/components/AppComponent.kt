package com.tnh.mollert.components

import android.content.Context
import com.tnh.mollert.datasource.AppRepository
import com.tnh.mollert.utils.FirestoreHelper
import com.tnh.mollert.utils.NotificationHelper
import com.tnh.mollert.utils.StorageHelper
import com.tnh.tnhlibrary.preference.PrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class AppComponent {
    @Provides
    fun par(@ApplicationContext context: Context): AppRepository{
        return AppRepository.getInstance(context)
    }

    @Provides
    fun ppm(@ApplicationContext context: Context): PrefManager{
        return PrefManager.getInstance(context)
    }

    @Provides
    fun pfh(): FirestoreHelper{
        return FirestoreHelper.getInstance()
    }

    @Provides
    fun psh(): StorageHelper{
        return StorageHelper.getInstance()
    }

    @Provides
    fun pnh(@ApplicationContext context: Context): NotificationHelper{
        return NotificationHelper.get(context)
    }

}