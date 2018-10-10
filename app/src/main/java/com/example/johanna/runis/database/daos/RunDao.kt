package com.example.johanna.runis.database.daos

import android.arch.persistence.room.*
import com.example.johanna.runis.database.entities.Run

@Dao
interface RunDao {
    @Query("SELECT * FROM run")
    fun getAll(): List<Run>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(run: Run)

    @Update
    fun update(run: Run)

    @Delete
    fun delete(run: Run)

    @Query("SELECT * FROM run " +
            "WHERE runid = :id LIMIT 1")
    fun getRunByd(id: Int): Run
}