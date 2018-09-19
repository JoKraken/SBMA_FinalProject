package com.example.johanna.runis

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class FragmentSettings: Fragment() {
    internal var activityCallBack: FragmentSettingsListener? = null

    interface FragmentSettingsListener {
        fun onSwipeRightSettings()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_setting, container, false)
        activityCallBack = context as FragmentSettingsListener

        rootView.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                Log.e("ViewSwipe", "Settings Right")
                activityCallBack!!.onSwipeRightSettings()
            }
        })
        return rootView
    }
}