package com.soni.appsanalyzer.di

import android.content.Context
import com.soni.appsanalyzer.data.repository.AppRepositoryImpl
import com.soni.appsanalyzer.domain.repository.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context
    ): AppRepository {
        return AppRepositoryImpl(context)
    }
}
