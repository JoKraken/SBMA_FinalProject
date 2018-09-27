package com.example.johanna.runis

import android.arch.persistence.room.*
import android.content.Context
import android.util.Log


//@Entity
//data class Run(
//        @PrimaryKey val runid: Int,
//        val time: Float, //time in minutes
//        val km: Float,
//        val date: String
//) {
//    //constructor, getter and setter are implicit :)
//    override fun toString(): String{
//        return "$runid:  $date, $time hour, $km"
//    }
//}
//
//@Entity(foreignKeys = [(ForeignKey(
//        entity = Run::class,
//        parentColumns = ["runid"],
//        childColumns = ["run"]))])
//data class RunDetails(
//        val run: Int,
//        val type: Int, //e.g. 1 = position, 2 = heartbeat
//        @PrimaryKey
//        val value: String
//){
//    //constructor, getter and setter are implicit :)
//    override fun toString(): String = "$run:   $type:   $value"
//}
//
//@Dao
//interface RunDetailsDao {
//    @Query("SELECT * FROM runDetails")
//    fun getAll(): List<RunDetailsDao>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insert(runDetail: RunDetailsDao)
//}
//
//@Dao
//interface RunDao {
//    @Query("SELECT * FROM run")
//    fun getAll(): List<Run>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insert(user: Run)
//
//    @Update
//    fun update(user: Run)
//
//    @Query("SELECT * FROM runDetails " +
//            "INNER JOIN run " +
//            "ON runDetail.runid = run.run " +
//            "WHERE runDetails.run = :runid")
//    fun getUserRuns(runid: Int): List<Run>
//}
//
//@Database(entities = [(RunDetails::class), (Run::class)], version = 1)
//abstract class UserDB: RoomDatabase() {
//    abstract fun runDetailsDao(): RunDetailsDao
//    abstract fun runDao(): RunDao
//    /* one and only one instance */
//    companion object {
//        private var sInstance: UserDB? = null
//        @Synchronized
//        fun get(context: Context): UserDB {
//            Log.d("DEBUG", context.applicationContext.toString())
//            if (sInstance == null) {
//                sInstance = Room.databaseBuilder(context.applicationContext, UserDB::class.java, "user.db")
//                        .fallbackToDestructiveMigration()
//                        .allowMainThreadQueries()
//                        .build()
//            }
//            return sInstance!!
//        }
//    }
//}