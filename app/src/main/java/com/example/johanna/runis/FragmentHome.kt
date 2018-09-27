package com.example.johanna.runis

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.FloatingActionButton



class FragmentHome : Fragment() {
    internal var activityCallBack: FragmentHomeListener? = null

    interface FragmentHomeListener {
        fun onSwipeLeftHome()
        fun newRun()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_home, container, false)
        activityCallBack = context as FragmentHomeListener

        rootView.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Home Left")
                activityCallBack!!.onSwipeLeftHome()
            }
        })

        val add = rootView.findViewById(R.id.add) as FloatingActionButton
        add.setOnClickListener {
            activityCallBack!!.newRun()
        }

        return rootView
    }

}