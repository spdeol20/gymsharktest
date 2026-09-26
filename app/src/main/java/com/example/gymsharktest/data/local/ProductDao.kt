package com.example.gymsharktest.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Matches one stored label inside the JSON column. Android SQLite on this minSdk has no
 * json_each table function, so the match is a literal search for the encoded key.
 * An empty [text] is a known label. Callers escape LIKE wildcards before binding.
 */
private const val LABEL_MATCH =
    "(" +
        "(:text = '' AND labelsJson LIKE '%\"key\":\"' || :key || '\"%' ESCAPE '\\') " +
        "OR (:text != '' AND labelsJson LIKE '%\"key\":\"unknown\",\"text\":\"' || :text || '\"%' ESCAPE '\\')" +
        ")"

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY position ASC")
    fun pagingCatalogue(): PagingSource<Int, ProductEntity>

    @Query("SELECT * FROM products ORDER BY amountMinorUnits ASC, position ASC")
    fun pagingPriceAsc(): PagingSource<Int, ProductEntity>

    @Query("SELECT * FROM products ORDER BY amountMinorUnits DESC, position ASC")
    fun pagingPriceDesc(): PagingSource<Int, ProductEntity>

    @Query(
        """
        SELECT * FROM products
        WHERE $LABEL_MATCH
        ORDER BY position ASC
        """,
    )
    fun pagingCatalogueByLabel(key: String, text: String): PagingSource<Int, ProductEntity>

    @Query(
        """
        SELECT * FROM products
        WHERE $LABEL_MATCH
        ORDER BY amountMinorUnits ASC, position ASC
        """,
    )
    fun pagingPriceAscByLabel(key: String, text: String): PagingSource<Int, ProductEntity>

    @Query(
        """
        SELECT * FROM products
        WHERE $LABEL_MATCH
        ORDER BY amountMinorUnits DESC, position ASC
        """,
    )
    fun pagingPriceDescByLabel(key: String, text: String): PagingSource<Int, ProductEntity>

    @Query(
        """
        SELECT COUNT(*) FROM products
        WHERE $LABEL_MATCH
        """,
    )
    fun observeCountByLabel(key: String, text: String): Flow<Int>

    @Query(
        """
        SELECT * FROM products
        WHERE inStock = 1 AND hasMerchandisingLabel = 1
        ORDER BY position ASC
        """,
    )
    fun observeFeatured(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun findById(id: Long): ProductEntity?

    @Query(
        """
        SELECT * FROM products
        WHERE inStock = 1 AND hasMerchandisingLabel = 1
        ORDER BY position ASC
        """,
    )
    suspend fun featured(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Long): Flow<ProductEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun deleteAll()

    /** Replace is atomic: a failed write leaves the previous catalogue on screen. */
    @Transaction
    suspend fun replaceAll(products: List<ProductEntity>) {
        deleteAll()
        insertAll(products)
    }
}
