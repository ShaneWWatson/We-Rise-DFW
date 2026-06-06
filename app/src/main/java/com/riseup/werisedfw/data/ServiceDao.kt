package com.riseup.werisedfw.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/** Room DAO for the local services cache. */
@Dao
interface ServiceDao {

    /** Returns every [Service] row in the database, in no guaranteed order. */
    @Query("SELECT * FROM services")
    suspend fun all(): List<Service>

    /**
     * Returns the [Service] with the given [id], or `null` if it does not exist.
     *
     * @param id Stable service identifier (e.g. `"food_north_texas_food_bank"` or `"osm_12345"`).
     */
    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getById(id: String): Service?

    /** Deletes every row in the `services` table. */
    @Query("DELETE FROM services")
    suspend fun clear()

    /**
     * Inserts or replaces all [items] in a single transaction.
     *
     * @param items Services to upsert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Service>)

    /**
     * Atomically replaces the entire table with [items].
     *
     * @param items The new complete set of services.
     */
    @Transaction
    suspend fun replaceAll(items: List<Service>) {
        clear()
        insertAll(items)
    }
}

