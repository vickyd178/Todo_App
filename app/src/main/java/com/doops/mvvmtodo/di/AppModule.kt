package com.doops.mvvmtodo.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.doops.mvvmtodo.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application, callback: TaskDatabase.CallBack) =
        Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
            .addMigrations(TaskDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides()
    fun provideTaskDao(db: TaskDatabase) =
        db.taskDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun providesApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope