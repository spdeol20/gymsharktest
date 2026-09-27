package com.example.gymsharktest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Transaction
    @Query("SELECT * FROM cart_lines ORDER BY addedAt DESC")
    fun observeLines(): Flow<List<StoredCartLine>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_lines")
    fun observeQuantity(): Flow<Long>

    @Query("SELECT * FROM cart_lines WHERE productId = :productId AND sizeKey = :sizeKey")
    suspend fun find(productId: Long, sizeKey: String): CartLineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(line: CartLineEntity)

    @Query("DELETE FROM cart_lines WHERE productId = :productId AND sizeKey = :sizeKey")
    suspend fun delete(productId: Long, sizeKey: String)

    /** Drop selections whose product left the catalogue. */
    @Query("DELETE FROM cart_lines WHERE productId NOT IN (SELECT id FROM products)")
    suspend fun deleteOrphans()
}
