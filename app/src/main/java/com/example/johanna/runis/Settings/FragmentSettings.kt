package com.example.johanna.runis.Settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import com.example.johanna.runis.OnSwipeTouchListener
import com.example.johanna.runis.R


class FragmentSettings: PreferenceFragmentCompat(){

    internal var activityCallBack: FragmentSettingsListener? = null

    interface FragmentSettingsListener {
        fun onSwipeRightSettings()
        fun stopPreference()
    }


    /*
    input: inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    output: View?
    description: create view, setOnTouchListener for swiping
    */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateRecyclerView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): RecyclerView {
        Log.d("DEBUG", "onCreateRecyclerView")
        val rootView =  super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        activityCallBack = context as FragmentSettingsListener

        rootView.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                Log.e("ViewSwipe", "Settings Right")
                activityCallBack!!.onSwipeRightSettings()
            }
        })

        return rootView
    }

    /*
    input: -
    output: void
    description: is called when preferences are created
    */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_setting)

    }

    /*
    input: -
    output: void
    description: is called when you leave the fragment and call the function stopPreference the save the preferences in the mainActivity
    */
    override fun onStop() {
        Log.d("DEBUG", "onStop")
        activityCallBack!!.stopPreference()
        super.onStop()
    }
}