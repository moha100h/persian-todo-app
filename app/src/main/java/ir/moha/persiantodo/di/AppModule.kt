package ir.moha.persiantodo.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.moha.persiantodo.data.local.AppDatabase
import ir.moha.persiantodo.data.local.dao.CategoryDao
import ir.moha.persiantodo.data.local.dao.TaskDao
import ir.moha.persiantodo.data.repository.TaskRepositoryImpl
import ir.moha.persiantodo.domain.repository.TaskRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "persian_todo.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides @Singleton
    fun provideTaskRepository(impl: TaskRepositoryImpl): TaskRepository = impl
}
