package com.example.gymsharktest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, CartLineEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class GymsharkDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    abstract fun cartDao(): CartDao
}
