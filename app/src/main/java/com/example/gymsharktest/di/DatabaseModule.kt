package com.example.gymsharktest.di

import android.content.Context
import androidx.room.Room
import com.example.gymsharktest.data.local.GymsharkDatabase
import com.example.gymsharktest.data.local.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * A schema change drops the table. The next refresh refills it from the CDN, which is safe
     * because the rows are a public catalogue cache and not user data.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymsharkDatabase =
        Room.databaseBuilder(context, GymsharkDatabase::class.java, "gymshark.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideProductDao(database: GymsharkDatabase): ProductDao = database.productDao()
}
