package com.example.gymsharktest.di

import android.content.Context
import androidx.room.Room
import com.example.gymsharktest.data.local.CartDao
import com.example.gymsharktest.data.local.GymsharkDatabase
import com.example.gymsharktest.data.local.MIGRATION_1_2
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
     * Catalogue rows are a public cache. Basket rows are a product id, a size, and a quantity.
     * They are not payment details.
     *
     * Version 1 to 2 creates the basket table and keeps the catalogue. A version jump with no
     * migration still drops every table. The next refresh refills the catalogue, and the basket
     * starts empty.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymsharkDatabase =
        Room.databaseBuilder(context, GymsharkDatabase::class.java, "gymshark.db")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideProductDao(database: GymsharkDatabase): ProductDao = database.productDao()

    @Provides
    fun provideCartDao(database: GymsharkDatabase): CartDao = database.cartDao()
}
