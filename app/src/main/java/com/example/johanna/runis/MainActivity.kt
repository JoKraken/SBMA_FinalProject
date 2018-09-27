package com.example.johanna.runis

import android.arch.persistence.room.*
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v4.app.Fragment
import android.support.v4.math.MathUtils
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.Toast
import com.example.johanna.runis.R.id.fragment_container
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), FragmentNewRun.FragmentNewRunListener, FragmentHome.FragmentHomeListener, FragmentSettings.FragmentSettingsListener, FragmentMyRuns.FragmentMyRunsListener{


    @Entity
    data class Run(
            @PrimaryKey val runid: Int,
            val time: String, //time in minutes
            val km: String,
            val date: String
    ) {
        //constructor, getter and setter are implicit :)
        override fun toString(): String{
            return "$runid:  $date, $time, $km"
        }
    }

    @Entity(foreignKeys = [(ForeignKey(
            entity = Run::class,
            parentColumns = ["runid"],
            childColumns = ["run"]))])
    data class RunDetails(
            val run: Int,
            val type: Int, //e.g. 1 = position, 2 = heartbeat
            @PrimaryKey
            val value: String
    ){
        //constructor, getter and setter are implicit :)
        override fun toString(): String = "$run:   $type:   $value"
    }

    @Dao
    interface RunDetailsDao {
        @Query("SELECT * FROM runDetails")
        fun getAll(): List<RunDetails>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(runDetail: RunDetails)
    }

    @Dao
    interface RunDao {
        @Query("SELECT * FROM run")
        fun getAll(): List<Run>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(run: Run)

        @Update
        fun update(run: Run)

        @Query("SELECT * FROM runDetails " +
                "INNER JOIN run " +
                "ON runDetails.run = run.runid " +
                "WHERE runDetails.run = :runid")
        fun getUserRuns(runid: Int): List<Run>
    }

    @Database(entities = [(Run::class), (RunDetails::class)], version = 1)
    abstract class RunDB: RoomDatabase() {
        abstract fun runDetailsDao(): RunDetailsDao
        abstract fun runDao(): RunDao
        /* one and only one instance */
        companion object {
            private var sInstance: RunDB? = null
            @Synchronized
            fun get(context: Context): RunDB {
                Log.d("DEBUG", context.applicationContext.toString())
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

    private var content: FrameLayout? = null
    private var navigation: BottomNavigationView? = null

    //Runs
    private var runID: Int = 1

    //chronometer
    private var newRun: Boolean = false
    private var chronometer: Chronometer? = null
    private var timer: Long = 0

    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                var fragment = Fragment()
                if(newRun) {
                    val bundle = Bundle()
                    bundle.putLong("timer", (SystemClock.elapsedRealtime() - chronometer!!.base))
                    fragment = FragmentNewRun()
                    fragment.setArguments(bundle);
                }else{
                    fragment = FragmentHome()
                }
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myRunns -> {
                val fragment = FragmentMyRuns()
                val db = RunDB.get(this)
                val runs = db.runDao().getAll()
                if(runs != null) {
                    fragment.listAdapter = RunListAdapter(this, runs)
                }else{
                    Log.d("DEBUG", "ListViewRuns == null: "+runs.toString())
                    Toast.makeText(this@MainActivity, "ListViewRuns == null: "+runs.toString(), Toast.LENGTH_SHORT).show()
                }

                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                addFragment(FragmentSettings())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNewTheme()
        setContentView(R.layout.activity_main)

        content = findViewById<FrameLayout>(R.id.fragment_container) as FrameLayout
        navigation = findViewById<BottomNavigationView>(R.id.navigation) as BottomNavigationView
        navigation!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val db = RunDB.get(this)
        db.runDao().insert(Run(0, "Time", "Km","Date"))

        val fragment = FragmentHome()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()

    }
    // set Theme
    private fun setNewTheme(){
        val prefManager = PreferenceManager.getDefaultSharedPreferences(this)
        val theme = prefManager.getString("list_preference_1", "1")
        if(theme.equals("1")){
            setTheme(android.R.style.Theme_Black_NoTitleBar);
        }else if(theme.equals("2")){
            setTheme(android.R.style.Theme_Light_NoTitleBar);
        }
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    //end run and stop chronometer
    override fun endRun(time: Long) {
        newRun = false

        val db = RunDB.get(this)
        var time = ""
        val minutes = TimeUnit.MILLISECONDS.toMinutes((SystemClock.elapsedRealtime()-chronometer!!.base))
        val secounds = TimeUnit.MILLISECONDS.toSeconds((SystemClock.elapsedRealtime()-chronometer!!.base))
        if(secounds < 10){
            time = minutes.toString()+":0"+secounds
        }else{
            time = minutes.toString()+":"+secounds
        }
        val c = Calendar.getInstance()
        val date = c.get(Calendar.DATE).toString()+"."+c.get(Calendar.MONTH).toString()+"."+c.get(Calendar.YEAR).toString()
        db.runDao().insert(Run(runID,  time, "0", date))
        runID = runID +1
        chronometer!!.setBase(SystemClock.elapsedRealtime())
        chronometer!!.stop()
        val fragment = FragmentHome()
        addFragment(fragment)
    }

    //start new run and start chronometer
    override fun newRun() {
        newRun = true

        chronometer = findViewById(R.id.chronometer)
        startChronometer()

        val fragment = FragmentNewRun()
        addFragment(fragment)
    }

    fun startChronometer() {
        if (!newRun) {
            chronometer!!.start()
            newRun = true
        }
    }

    override fun onListClick(position: Int){
        val db = RunDB.get(this)
        val run = db.runDao().getAll()[position]
        Toast.makeText(this, run.toString(), Toast.LENGTH_SHORT).show()
    }

    //get settings
    override fun stopPreference() {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(this)
        val gps = prefManager.getBoolean("switch_gps", true)
        val bluetooth = prefManager.getBoolean("switch_bluetooth", true)
        val name = prefManager.getString("edit_name", "name")
        Log.d("DEBUG", "gps: " + gps + ", bluetooth: "+bluetooth+ ", name: "+name)
    }

    //function for swipen the menu
    override fun onSwipeLeftNewRun(time: Long) {
        timer = time
        navigation!!.selectedItemId = R.id.navigation_myRunns
    }
    override fun onSwipeLeftHome() {
        navigation!!.selectedItemId = R.id.navigation_myRunns
    }

    override fun onSwipeRightSettings() {
        navigation!!.selectedItemId = R.id.navigation_myRunns
    }

    override fun onSwipeRightMyRuns() {
        navigation!!.selectedItemId = R.id.navigation_home
    }

    override fun onSwipeLeftMyRuns() {
        navigation!!.selectedItemId = R.id.navigation_settings
    }

}