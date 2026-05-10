package com.werisetech.weriseapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/** Room DAO for the local services cache. */
@Dao
interface ServiceDao {

    @Query("SELECT * FROM services WHERE category = :category")
    suspend fun byCategory(category: Category): List<Service>

    @Query("SELECT * FROM services")
    suspend fun all(): List<Service>

    @Query("DELETE FROM services")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Service>)

    /** Atomic clear-then-insert. */
    @Transaction
    suspend fun replaceAll(items: List<Service>) {
        clear()
        insertAll(items)
    }
}
