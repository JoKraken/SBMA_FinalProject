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
class MainActivity : AppCompatActivity(), FragmentNewRun.FragmentNewRunListener, FragmentHomeFirst.FragmentHomeFirstListener, FragmentHome.FragmentHomeListener, FragmentSettings.FragmentSettingsListener, FragmentMyRuns.FragmentMyRunsListener{


    @Entity
    data class Run(
            @PrimaryKey val runid: Int,
            val time: String, //time in minutes
            val km: String,
            val date: String,
            val date_base: Long
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

    @Database(entities = [(Run::class), (RunDetails::class)], version = 2)
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
                val bundle = Bundle()
                if(newRun) {
                    bundle.putLong("timer", (SystemClock.elapsedRealtime() - chronometer!!.base))
                    fragment = FragmentNewRun()
                    fragment.setArguments(bundle)
                }else{
                    Log.d("DEBUG_main", "newRun false")
                    val db = RunDB.get(this)
                    Log.d("DEBUG_main", db.runDao().getAll().size.toString())
                    if(runID != 1){
                        Log.d("DEBUG_main", "size != 0")
                        val run = db.runDao().getAll()[db.runDao().getAll().size-1]
                        var array = Array<String>(5){""}
                        array[0] = run.date
                        array[1] = run.km
                        array[2] = run.time
                        bundle.putStringArray("details", array)
                        fragment = FragmentHome()
                    }else{
                        fragment = FragmentHomeFirst()
                    }

                }
                getSupportActionBar()!!.setTitle(R.string.title_home)
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myRunns -> {
                val fragment = FragmentMyRuns()
                val db = RunDB.get(this)
                val runs = db.runDao().getAll()
                if(runs != null) {
                    fragment.listAdapter = RunListAdapter(this, runs)
                    val bundle = Bundle()
                    bundle.putString("totalTime", timeToString(getTotalTime()))
                    bundle.putDouble("totalKm", getAllKm())
                    fragment.setArguments(bundle);
                }else{
                    Log.d("DEBUG", "ListViewRuns == null: "+runs.toString())
                    Toast.makeText(this@MainActivity, "ListViewRuns == null: "+runs.toString(), Toast.LENGTH_SHORT).show()
                }

                getSupportActionBar()!!.setTitle(R.string.title_myRunns)
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                getSupportActionBar()!!.setTitle(R.string.title_setting)
                addFragment(FragmentSettings())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    //get total time from the user
    fun getTotalTime(): Long {
        val db = RunDB.get(this)
        val runs = db.runDao().getAll()
        var totalTime = runs[0].date_base
        for (run in runs){
            if(run.date_base != totalTime){
                Log.d("DEBUG_main", run.date_base.toString())
                totalTime += run.date_base
            }else{
                Log.d("DEBUG_main", run.km)
            }
        }
        return totalTime
    }

    //get all km from the user
    fun getAllKm(): Double {
        val db = RunDB.get(this)
        val runs = db.runDao().getAll()
        var totalKm = 0.0
        for (run in runs){
            if(!run.km.equals("Km")){
                Log.d("DEBUG_main", java.lang.Double.valueOf(run.km).toString())
                totalKm += java.lang.Double.valueOf(run.km)
            }else{
                Log.d("DEBUG_main", run.km)
            }
        }
        return totalKm
    }

    fun timeToString(time: Long): String{
        var timeString = ""
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time)
        val time2 = (time - (minutes*60000))
        val secounds = TimeUnit.MILLISECONDS.toSeconds(time2)
        if(secounds < 10){
            timeString = minutes.toString()+":0"+secounds
        }else{
            timeString = minutes.toString()+":"+secounds
        }
        return timeString
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNewTheme()
        setContentView(R.layout.activity_main)
        getSupportActionBar()!!.setTitle(R.string.title_home)

        content = findViewById<FrameLayout>(R.id.fragment_container) as FrameLayout
        navigation = findViewById<BottomNavigationView>(R.id.navigation) as BottomNavigationView
        navigation!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val db = RunDB.get(this)
        db.runDao().insert(Run(0, "Time", "Km","Date", 0))

        var fragment = Fragment()
        if(runID == 1){
            fragment = FragmentHomeFirst()
        }else{
            fragment = FragmentHome()
        }
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
        var time = timeToString((SystemClock.elapsedRealtime()-chronometer!!.base))
        val c = Calendar.getInstance()
        val date = c.get(Calendar.DATE).toString()+"."+c.get(Calendar.MONTH).toString()+"."+c.get(Calendar.YEAR).toString()
        db.runDao().insert(Run(runID,  time, "0.0", date, (SystemClock.elapsedRealtime()-chronometer!!.base)))
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
    override fun newRunFirst() {
        newRun()
    }

    override fun connectBT(){
        val fragment = BluetoothFragment()
        addFragment(fragment)
    }

    override fun connectBTFirst(){
        val fragment = BluetoothFragment()
        addFragment(fragment)
    }

    fun startChronometer() {
        if (!newRun) {
            chronometer!!.start()
            newRun = true
        }
    }

    //create rundetails fragment and put the information in bundle
    override fun onListClick(position: Int){
        val db = RunDB.get(this)
        val run = db.runDao().getAll()[position]
        val bundle = Bundle()
        var array = Array<String>(5){""}
        array[0] = run.date
        array[1] = run.km
        array[2] = run.time
        bundle.putStringArray("details", array)
        var fragment = FragmentRunDetails()
        fragment.setArguments(bundle)
        getSupportActionBar()!!.setTitle(R.string.title_runDetails)
        addFragment(fragment)
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
        Log.d("DEBUG_main", time.toString())
        navigation!!.selectedItemId = R.id.navigation_myRunns
    }

    override fun onSwipeLeftHomeFirst() {
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
