package com.example.johanna.runis

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView


class FragmentSettings: PreferenceFragmentCompat(){

    internal var activityCallBack: FragmentSettingsListener? = null

    interface FragmentSettingsListener {
        fun onSwipeRightSettings()
        fun stopPreference()
    }

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

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_setting)

    }

    override fun onStop() {
        Log.d("DEBUG", "onStop")
        activityCallBack!!.stopPreference()
        super.onStop()
    }
}