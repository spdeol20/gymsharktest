package com.example.gymsharktest.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Creates the basket table. The catalogue table is left as it is, so an upgrade does not throw
 * away cached products or ask the shopper to rebuild a basket that did not exist yet.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cart_lines` (" +
                "`productId` INTEGER NOT NULL, " +
                "`sizeKey` TEXT NOT NULL, " +
                "`quantity` INTEGER NOT NULL, " +
                "`addedAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`productId`, `sizeKey`))",
        )
    }
}
