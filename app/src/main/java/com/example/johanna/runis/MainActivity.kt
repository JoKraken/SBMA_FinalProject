package com.example.johanna.runis

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.Toast
import com.example.johanna.runis.Home.FragmentHome
import com.example.johanna.runis.Home.FragmentHomeFirst
import com.example.johanna.runis.Home.FragmentNewRun
import com.example.johanna.runis.MyRuns.FragmentMyRuns
import com.example.johanna.runis.MyRuns.FragmentRunDetails
import com.example.johanna.runis.MyRuns.RunListAdapter
import com.example.johanna.runis.Settings.FragmentSettings
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.johanna.runis.database.RunDB
import com.example.johanna.runis.database.entities.Run
import com.google.gson.Gson
import org.osmdroid.util.GeoPoint
import kotlin.collections.ArrayList
import android.arch.persistence.room.Delete


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), FragmentRunDetails.FragmentRunDetailsListener, FragmentNewRun.FragmentNewRunListener, FragmentHomeFirst.FragmentHomeFirstListener, FragmentHome.FragmentHomeListener, FragmentSettings.FragmentSettingsListener, FragmentMyRuns.FragmentMyRunsListener{

    private var content: FrameLayout? = null
    private var navigation: BottomNavigationView? = null

    //Runs
    private var runID: Int = 1

    //chronometer
    private var newRun: Boolean = false
    private var chronometer: Chronometer? = null
    private var timer: Long = 0
    private var user = Array<String>(3){""} //gps, bluetooth, name

    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                var fragment = Fragment()
                val bundle = Bundle()
                if(newRun) {
                    Log.d("DEBUG_main_newRun", chronometer!!.base.toString())
                    bundle.putLong("timer", (SystemClock.elapsedRealtime() - chronometer!!.base))
                    fragment = FragmentNewRun()
                    fragment.setArguments(bundle)
                }else{
                    Log.d("DEBUG_main", "newRun false")
                    fragment = getHomeFragment()
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
                    Log.d("DEBUG_main", "ListViewRuns == null: "+runs.toString())
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

    fun getHomeFragment(): Fragment{
        var fragment = Fragment()
        val bundle = Bundle()
        val db = RunDB.get(this)
        Log.d("DEBUG_main", db.runDao().getAll().size.toString())
        if(db.runDao().getAll().size > 1){
            Log.d("DEBUG_main", "size != 0")
            val run = db.runDao().getAll()[db.runDao().getAll().size-1]
            var array = Array<String>(5){""}
            array[0] = run.date
            array[1] = run.km
            array[2] = run.time
            array[3] = user[2]
            bundle.putStringArray("details", array)
            fragment = FragmentHome()
            fragment.setArguments(bundle);
        }else{
            fragment = FragmentHomeFirst()
        }
        return fragment
    }

    //get total time from the user
    fun getTotalTime(): Long {
        val db = RunDB.get(this)
        val runs = db.runDao().getAll()
        var totalTime = runs[0].date_milisecound
        for (run in runs){
            if(run.date_milisecound != totalTime){
                //Log.d("DEBUG_main", run.date_milisecound.toString())
                totalTime += run.date_milisecound
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
            if(!run.km.equals("Km") && !run.km.equals("-")){
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

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        content = findViewById<FrameLayout>(R.id.fragment_container) as FrameLayout
        navigation = findViewById<BottomNavigationView>(R.id.navigation) as BottomNavigationView
        navigation!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        stopPreference()

        val db = RunDB.get(this)
        db.runDao().insert(Run(0, "Time", "Km","Date", 0, 0))
        runID = db.runDao().getAll().size

        var fragment = Fragment()
        Log.d("DEBUG_main_create", runID.toString())
        Log.d("DEBUG_main_create", db.runDao().getAll().toString())
        if(runID > 1 && db.runDao().getAll()[runID-1].date_milisecound == 0L){
            Log.d("DEBUG_main_create", "base == 0")
            chronometer = findViewById(R.id.chronometer)
            chronometer!!.setBase(db.runDao().getAll()[runID-1].date_base)
            chronometer!!.start()

            val bundle = Bundle()
            bundle.putLong("timer", (SystemClock.elapsedRealtime() - chronometer!!.base))
            fragment = FragmentNewRun()
            fragment.setArguments(bundle)

            newRun = true
        }else{
            Log.d("DEBUG_main_create", "base != 0")
            fragment = getHomeFragment()
        }

        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).addToBackStack(null).commit()

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
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
    }

    //end run and stop chronometer
    override fun endRun(time: Long, runRoute: RunRoute, length: Double) {
        newRun = false

        val db = RunDB.get(this)
        var time = timeToString((SystemClock.elapsedRealtime()-chronometer!!.base))
        val c = Calendar.getInstance()
        val date = c.get(Calendar.DATE).toString()+"."+c.get(Calendar.MONTH).toString()+"."+c.get(Calendar.YEAR).toString()
        db.runDao().insert(Run(runID,  time, "0.0", date, chronometer!!.base,(SystemClock.elapsedRealtime()-chronometer!!.base)))
        runID = runID +1
        chronometer!!.setBase(SystemClock.elapsedRealtime())
        chronometer!!.stop()
        val fragment = getHomeFragment()
        addFragment(fragment)
    }

    //start new run and start chronometer
    override fun newRun() {
        chronometer = findViewById(R.id.chronometer)

        if(startChronometer()){
            navigation!!.selectedItemId = R.id.navigation_home
        }else{
            Toast.makeText(this@MainActivity, getString(R.string.main_Toast_noRun), Toast.LENGTH_SHORT).show()
        }
    }

    fun startChronometer(): Boolean {
        val db = RunDB.get(this)
        val size = db.runDao().getAll().size-1
        if((db.runDao().getAll()[size].date_base == 0L && !newRun )|| db.runDao().getAll()[size].date_milisecound != 0L){
            chronometer!!.setBase(SystemClock.elapsedRealtime())
            chronometer!!.start()
            newRun = true

            val c = Calendar.getInstance()
            val date = c.get(Calendar.DATE).toString()+"."+c.get(Calendar.MONTH).toString()+"."+c.get(Calendar.YEAR).toString()
            db.runDao().insert(Run(runID,  "-", "-", date, chronometer!!.base, 0))
            return true
        }else{
            return false
        }

    }

    //create rundetails fragment and put the information in bundle
    override fun onListClick(position: Int){
        val db = RunDB.get(this)
        val run = db.runDao().getAll()[position]
        if(run.date_milisecound != 0L){
            val bundle = Bundle()
            var array = Array<String>(5){""}
            array[0] = run.date
            array[1] = run.km
            array[2] = run.time
            array[3] = run.runid.toString()
            bundle.putStringArray("details", array)
            var fragment = FragmentRunDetails()
            fragment.setArguments(bundle)
            getSupportActionBar()!!.setTitle(R.string.title_runDetails)
            addFragment(fragment)
        }else{
            Toast.makeText(this@MainActivity, getString(R.string.main_Toast_noRunDetails), Toast.LENGTH_SHORT).show()
        }
    }

    //get settings
    override fun stopPreference() {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(this)
        val gps = prefManager.getBoolean("switch_gps", true)
        //val bluetooth = prefManager.getBoolean("switch_bluetooth", true)
        val name = prefManager.getString("edit_name", "name")
        Log.d("DEBUG_main", "gps: " + gps + ", name: "+name)
        user[0] = gps.toString()
        //user[1] = bluetooth.toString()
        user[2] = name
    }

    //delete Run
    override fun deleteRun(id: Int){
        Log.d("DEBUG_main", "delete")
        val db = RunDB.get(this)
        val run = db.runDao().getRunByd(id)
        Log.d("DEBUG_main", run.toString())
        db.runDao().delete(run)
        Toast.makeText(this@MainActivity, getString(R.string.delete_Toast), Toast.LENGTH_SHORT).show()
        navigation!!.selectedItemId = R.id.navigation_myRunns
    }

    //function for swipen the menu
    override fun onSwipeLeftNewRun() {
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
