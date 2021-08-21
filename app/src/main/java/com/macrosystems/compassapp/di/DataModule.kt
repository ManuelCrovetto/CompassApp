package com.macrosystems.compassapp.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.macrosystems.compassapp.data.local.NavigationDetailsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun providesRoomDao(context: Application): NavigationDetailsDatabase =
        Room.databaseBuilder(context, NavigationDetailsDatabase::class.java, "navigation_details_database").build()
}