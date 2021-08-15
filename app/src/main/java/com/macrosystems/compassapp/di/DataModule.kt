package com.macrosystems.compassapp.di

import android.content.Context
import com.macrosystems.compassapp.data.network.repository.Repository
import com.macrosystems.compassapp.data.network.RepositoryImpl
import com.macrosystems.compassapp.data.network.persistence.Persistence
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
    fun providesPersistence(@ApplicationContext appContext: Context) = Persistence(appContext)

    @Provides
    @Singleton
    fun providesRepository(@ApplicationContext appContext: Context, persistence: Persistence): Repository {
        return RepositoryImpl(appContext, persistence)
    }

}