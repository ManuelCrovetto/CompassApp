package com.macrosystems.compassapp.di

import android.app.Activity
import android.content.Context
import com.macrosystems.compassapp.data.network.Repository
import com.macrosystems.compassapp.data.network.RepositoryImpl
import com.macrosystems.compassapp.ui.core.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {


    @Provides
    @Singleton
    fun providesRepository(@ApplicationContext appContext: Context): Repository {
        return RepositoryImpl(appContext)
    }



}