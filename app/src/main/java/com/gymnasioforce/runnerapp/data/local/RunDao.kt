package com.gymnasioforce.runnerapp.data.local

import androidx.room.*

@Dao
interface RunDao {

    @Query("SELECT * FROM runs ORDER BY createdAt DESC")
    suspend fun getAll(): List<RunEntity>

    @Query("SELECT * FROM runs WHERE id = :id")
    suspend fun getById(id: Int): RunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(runs: List<RunEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: RunEntity)

    @Delete
    suspend fun delete(run: RunEntity)

    @Query("DELETE FROM runs")
    suspend fun deleteAll()
}
