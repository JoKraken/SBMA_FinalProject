package com.example.johanna.runis

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.FloatingActionButton
import android.widget.TextView


class FragmentHomeFirst : Fragment() {
    internal var activityCallBack: FragmentHomeFirstListener? = null

    interface FragmentHomeFirstListener {
        fun onSwipeLeftHomeFirst()
        fun newRunFirst()
        fun connectBTFirst()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView : View? = inflater!!.inflate(R.layout.fragment_home_first, container, false)
        activityCallBack = context as FragmentHomeFirstListener

        rootView!!.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Home Left")
                activityCallBack!!.onSwipeLeftHomeFirst()
            }
        })

        val add = rootView.findViewById<FloatingActionButton>(R.id.add)
        val bt = rootView.findViewById<FloatingActionButton>(R.id.startBlueTooth)
        add.setOnClickListener {
            activityCallBack!!.newRunFirst()
        }
        bt.setOnClickListener {
            activityCallBack!!.connectBTFirst()
        }

        return rootView
    }

}