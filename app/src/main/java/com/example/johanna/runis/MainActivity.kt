package com.example.johanna.runis

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), FragmentHome.FragmentHomeListener, FragmentSettings.FragmentSettingsListener, FragmentMyRuns.FragmentMyRunsListener{

    private var content: FrameLayout? = null
    private var navigation: BottomNavigationView? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                val fragment = FragmentHome()
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myRunns -> {
                val fragment = FragmentMyRuns()
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentSettings()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNewTheme()
        setContentView(R.layout.activity_main)

        content = findViewById(R.id.fragment_container) as FrameLayout
        navigation = findViewById(R.id.navigation) as BottomNavigationView
        navigation!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        val fragment = FragmentHome()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
    }

    private fun setNewTheme(){
        Log.d("DEBUG", "setNewTheme")
        val prefManager = PreferenceManager.getDefaultSharedPreferences(this)
        Log.d("DEBUG", "setNewTheme2")
        val theme = prefManager.getString("list_preference_1", "1")
        Log.d("DEBUG", "setNewTheme3")
        if(theme.equals("1")){
            Log.d("DEBUG", "setNewTheme4")
            setTheme(android.R.style.Theme_Black_NoTitleBar);
        }else if(theme.equals("2")){
            Log.d("DEBUG", "setNewTheme5")
            setTheme(android.R.style.Theme_Light_NoTitleBar);
        }
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    //get settings
    override fun stopPreference() {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(this)
        val gps = prefManager.getBoolean("switch_gps", true)
        val bluetooth = prefManager.getBoolean("switch_bluetooth", true)
        val name = prefManager.getString("edit_name", "name")
        Log.d("DEBUG", "gps: " + gps + ", bluetooth: "+bluetooth+ ", name: "+name)
    }

    //function for swipen
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
