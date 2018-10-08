package com.example.johanna.runis.Home

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.FloatingActionButton
import com.example.johanna.runis.OnSwipeTouchListener
import com.example.johanna.runis.R


class FragmentHomeFirst : Fragment() {
    internal var activityCallBack: FragmentHomeFirstListener? = null

    interface FragmentHomeFirstListener {
        fun onSwipeLeftHomeFirst()
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

        return rootView
    }

}