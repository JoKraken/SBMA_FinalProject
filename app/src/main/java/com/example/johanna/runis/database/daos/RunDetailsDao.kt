package com.example.johanna.runis.database.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.example.johanna.runis.database.entities.RunDetails

@Dao
interface RunDetailsDao {
    @Query("SELECT * FROM runDetails ")
    fun getAll(): List<RunDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(runDetail: RunDetails)
}