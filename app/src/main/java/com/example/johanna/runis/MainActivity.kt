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


@Suppress("DEPRECATION", "SENSELESS_COMPARISON", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NAME_SHADOWING", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), FragmentRunDetails.FragmentRunDetailsListener, FragmentNewRun.FragmentNewRunListener, FragmentHomeFirst.FragmentHomeFirstListener, FragmentHome.FragmentHomeListener, FragmentSettings.FragmentSettingsListener, FragmentMyRuns.FragmentMyRunsListener{

    private var content: FrameLayout? = null
    private var navigation: BottomNavigationView? = null

    //Runs
    private var runID: Int = 1

    //chronometer
    private var newRun: Boolean = false
    private var chronometer: Chronometer? = null
    private var user = Array(3){""} //gps, bluetooth, name

    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                val fragment: Fragment
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
                supportActionBar!!.setTitle(R.string.title_home)
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
                    fragment.arguments = bundle
                }else{
                    Log.d("DEBUG_main", "ListViewRuns == null: "+runs.toString())
                    Toast.makeText(this@MainActivity, "ListViewRuns == null: "+runs.toString(), Toast.LENGTH_SHORT).show()
                }

                supportActionBar!!.setTitle(R.string.title_myRunns)
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                supportActionBar!!.setTitle(R.string.title_setting)
                addFragment(FragmentSettings())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    /*
    input: -
    output: Fragment
    description: check if you need the FragmentHomeFirst or the FragmentHome
    */
    private fun getHomeFragment(): Fragment{
        val fragment : Fragment
        val bundle = Bundle()
        val db = RunDB.get(this)
        Log.d("DEBUG_main", db.runDao().getAll().size.toString())
        if(db.runDao().getAll().size > 1){
            Log.d("DEBUG_main", "size != 0")
            val run = db.runDao().getAll()[db.runDao().getAll().size-1]
            val array = Array(5){""}
            array[0] = run.date
            array[1] = run.km
            array[2] = run.time
            array[3] = this.user[2]
            bundle.putStringArray("details", array)
            fragment = FragmentHome()
            fragment.setArguments(bundle)
        }else{
            fragment = FragmentHomeFirst()
        }
        return fragment
    }

    /*
    input: -
    output: Long
    description: get total time from the user
    */
    private fun getTotalTime(): Long {
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

    /*
    input: -
    output: Double
    description: get all km from the user
    */
    private fun getAllKm(): Double {
        val db = RunDB.get(this)
        val runs = db.runDao().getAll()
        var totalKm = 0.0
        for (run in runs){
            if(run.km != "Km" && run.km != "-"){
                Log.d("DEBUG_main", java.lang.Double.valueOf(run.km).toString())
                totalKm += java.lang.Double.valueOf(run.km)
            }else{
                Log.d("DEBUG_main", run.km)
            }
        }
        return totalKm
    }

    /*
    input: time: Long
    output: String
    description: calculate from milliseconds seconds and minutes and return a String
    */
    private fun timeToString(time: Long): String{
        val timeString: String
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time)
        val time2 = (time - (minutes*60000))
        val secounds = TimeUnit.MILLISECONDS.toSeconds(time2)
        timeString = if(secounds < 10){
            minutes.toString()+":0"+secounds
        }else{
            minutes.toString()+":"+secounds
        }
        return timeString
    }

    /*
    input: savedInstanceState: Bundle?
    output: void
    description: create everything
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNewTheme()
        setContentView(R.layout.activity_main)
        supportActionBar!!.setTitle(R.string.title_home)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        content = findViewById(R.id.fragment_container)
        navigation = findViewById(R.id.navigation)
        navigation!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        stopPreference()

        val db = RunDB.get(this)
        db.runDao().insert(Run(0, "Time", "Km","Date", 0, 0, Gson().toJson(null, RunRoute::class.java)))
        runID = db.runDao().getAll().size

        val fragment: Fragment
        Log.d("DEBUG_main_create", runID.toString())
        Log.d("DEBUG_main_create", db.runDao().getAll().toString())
        if(runID > 1 && db.runDao().getAll()[runID-1].date_milisecound == 0L){
            Log.d("DEBUG_main_create", "base == 0")
            chronometer = findViewById(R.id.chronometer)
            chronometer!!.base = db.runDao().getAll()[runID-1].date_base
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

    /*
    input: -
    output: void
    description: set Theme
    */
    private fun setNewTheme(){
        val prefManager = PreferenceManager.getDefaultSharedPreferences(this)
        val theme = prefManager.getString("list_preference_1", "1")
        if (theme == "1") {
            this.setTheme(android.R.style.Theme_Black_NoTitleBar)
        } else if(theme == "2"){
            this.setTheme(android.R.style.Theme_Light_NoTitleBar)
        }
    }

    /*
    input: fragment: Fragment
    output: void
    description: add fragment
    */
    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
    }

    /*
    input: time: Long, runRoute: RunRoute, length: Double
    output: void
    description: end run and stop chronometer
    */
    override fun endRun(time: Long, runRoute: RunRoute, length: Double) {
        newRun = false

        val db = RunDB.get(this)
        val time = timeToString((SystemClock.elapsedRealtime()-chronometer!!.base))
        val c = Calendar.getInstance()
        val date = c.get(Calendar.DATE).toString()+"."+c.get(Calendar.MONTH).toString()+"."+c.get(Calendar.YEAR).toString()
        db.runDao().update(Run(runID,  time, length.toString(), date, chronometer!!.base, (SystemClock.elapsedRealtime()-chronometer!!.base), Gson().toJson(runRoute)))
        runID += 1
        chronometer!!.base = SystemClock.elapsedRealtime()
        chronometer!!.stop()
        val fragment = getHomeFragment()
        addFragment(fragment)
    }

    /*
    input: -
    output: void
    description: check if the GPS is switched on and if the chronomater is started. yes ->change fragment no->show Toast
    */
    override fun newRun() {
        chronometer = findViewById(R.id.chronometer)

        if(user[0] == "true"){
            if(startChronometer()){
                navigation!!.selectedItemId = R.id.navigation_home
            }else{
                Toast.makeText(this@MainActivity, this.getString(R.string.main_Toast_noRun), Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this@MainActivity, this.getString(R.string.main_Toast_noRunGPS), Toast.LENGTH_SHORT).show()
        }

    }

    /*
    input: -
    output: boolean
    description: return if the chronometer is started
    */
    private fun startChronometer(): Boolean {
        val db = RunDB.get(this)
        val size = db.runDao().getAll().size-1
        return if((db.runDao().getAll()[size].date_base == 0L && !newRun )|| db.runDao().getAll()[size].date_milisecound != 0L){
            chronometer!!.base = SystemClock.elapsedRealtime()
            chronometer!!.start()
            newRun = true

            val c = Calendar.getInstance()
            val date = c.get(Calendar.DATE).toString()+"."+c.get(Calendar.MONTH).toString()+"."+c.get(Calendar.YEAR).toString()
            db.runDao().insert(Run(runID,  "-", "-", date, chronometer!!.base, 0, Gson().toJson(null, RunRoute::class.java)))
            true
        }else{
            false
        }

    }

    /*
    input: position: int
    output: void
    description: create rundetails fragment and put the information in bundle when date_milisecound != 0
    */
    override fun onListClick(position: Int){
        val db = RunDB.get(this)
        val run = db.runDao().getAll()[position]
        if(run.date_milisecound != 0L){
            val bundle = Bundle()
            val array = Array(5){""}
            array[0] = run.date
            array[1] = run.km
            array[2] = run.time
            array[3] = run.runid.toString()
            array[4] = run.runroute
            bundle.putStringArray("details", array)
            val fragment = FragmentRunDetails()
            fragment.arguments = bundle
            supportActionBar!!.setTitle(R.string.title_runDetails)
            addFragment(fragment)
        }else{
            Toast.makeText(this@MainActivity, getString(R.string.main_Toast_noRunDetails), Toast.LENGTH_SHORT).show()
        }
    }

    /*
    input: -
    output: void
    description: get settings and save them in user
    */
    override fun stopPreference() {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(this)
        val gps = prefManager.getBoolean("switch_gps", true)
        var name = prefManager.getString("edit_name", "name")
        if (name == "name") name = ""
        Log.d("DEBUG_main", "gps: $gps, name: $name")
        user[0] = gps.toString()
        user[2] = name
    }

    /*
    input: -
    output: void
    description: delete Run and show a Toast
    */
    override fun deleteRun(id: Int){
        Log.d("DEBUG_main", "delete")
        val db = RunDB.get(this)
        val run = db.runDao().getRunByd(id)
        Log.d("DEBUG_main", run.toString())
        db.runDao().delete(run)
        Toast.makeText(this@MainActivity, getString(R.string.delete_Toast), Toast.LENGTH_SHORT).show()
        navigation!!.selectedItemId = R.id.navigation_myRunns
    }

    /*
    input: -
    output: void
    description: function for swipen the menu
    */
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
