package com.example.taskapp.di

import android.content.Context
import androidx.room.Room
import com.example.taskapp.data.local.TaskDao
import com.example.taskapp.data.local.TaskDatabase
import com.example.taskapp.data.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): TaskDatabase {
        return Room.databaseBuilder(context, TaskDatabase::class.java, "task_db").build()
    }

    @Provides
    fun provideTaskDao(db: TaskDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideRepository(taskDao: TaskDao): TaskRepository = TaskRepository(taskDao)
}