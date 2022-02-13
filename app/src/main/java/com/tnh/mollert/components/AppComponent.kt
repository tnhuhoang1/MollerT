package com.tnh.mollert.components

import android.content.Context
import com.tnh.mollert.datasource.AppRepository
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

}