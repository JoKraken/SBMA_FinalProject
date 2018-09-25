package com.example.johanna.runis

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.FloatingActionButton

class  FragmentNewRun: Fragment() {
        internal var activityCallBack: FragmentNewRunListener? = null

        interface FragmentNewRunListener {
            fun onSwipeLeftNewRun()
            fun endRun()
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            var rootView = inflater!!.inflate(R.layout.fragment_newrun, container, false)
            activityCallBack = context as FragmentNewRunListener

            rootView.setOnTouchListener(object : OnSwipeTouchListener() {
                override fun onSwipeLeft() {
                    Log.e("ViewSwipe", "Home Left")
                    activityCallBack!!.onSwipeLeftNewRun()
                }
            })

            val check = rootView.findViewById(R.id.check) as FloatingActionButton
            check.setOnClickListener {
                activityCallBack!!.endRun()
            }

            return rootView
        }
}