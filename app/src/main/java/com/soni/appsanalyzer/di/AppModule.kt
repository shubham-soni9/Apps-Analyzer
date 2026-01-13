package com.soni.appsanalyzer.di

import android.content.Context
import androidx.room.Room
import com.soni.appsanalyzer.data.local.AppDao
import com.soni.appsanalyzer.data.local.AppDatabase
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "apps_db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Provides
    @Singleton
    fun provideAppDao(appDatabase: AppDatabase): AppDao {
        return appDatabase.appDao()
    }

    @Provides
    @Singleton
    fun provideAppRepository(@ApplicationContext context: Context, appDao: AppDao): AppRepository {
        return AppRepositoryImpl(context, appDao)
    }
}
