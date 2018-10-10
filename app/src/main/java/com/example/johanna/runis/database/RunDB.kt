package com.example.johanna.runis.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.util.Log
import com.example.johanna.runis.database.daos.RunDao
import com.example.johanna.runis.database.daos.RunDetailsDao
import com.example.johanna.runis.database.entities.Run
import com.example.johanna.runis.database.entities.RunDetails

@Database(entities = [(Run::class), (RunDetails::class)], version = 5)
abstract class RunDB: RoomDatabase() {
    abstract fun runDetailsDao(): RunDetailsDao
    abstract fun runDao(): RunDao
    /* one and only one instance */
    companion object {
        private var sInstance: RunDB? = null
        @Synchronized
        fun get(context: Context): RunDB {
            Log.d("DEBUG_main_database", context.applicationContext.toString())
            if (sInstance == null) {
                sInstance = Room.databaseBuilder(context.applicationContext, RunDB::class.java, "run.db")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
            }
            return sInstance!!
        }
    }
}