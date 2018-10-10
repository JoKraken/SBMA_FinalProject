package com.example.johanna.runis.database

import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.arch.persistence.room.DatabaseConfiguration
import android.arch.persistence.room.InvalidationTracker
import com.example.johanna.runis.database.daos.RunDao
import com.example.johanna.runis.database.daos.RunDetailsDao

class RunDBImpl : RunDB() {

    override fun runDetailsDao(): RunDetailsDao {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearAllTables() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createOpenHelper(config: DatabaseConfiguration?): SupportSQLiteOpenHelper {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun runDao(): RunDao {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}