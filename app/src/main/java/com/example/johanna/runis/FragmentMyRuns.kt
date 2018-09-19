package com.example.johanna.runis

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class FragmentMyRuns: Fragment() {
    internal var activityCallBack: FragmentMyRunsListener? = null

    interface FragmentMyRunsListener {
        fun onSwipeRightMyRuns()
        fun onSwipeLeftMyRuns()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_myruns, container, false)
        activityCallBack = context as FragmentMyRunsListener

        rootView.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "MyRuns Left")
                activityCallBack!!.onSwipeLeftMyRuns()
            }

            override fun onSwipeRight() {
                Log.e("ViewSwipe", "MyRuns Right")
                activityCallBack!!.onSwipeRightMyRuns()
            }
        })
        return rootView
    }

}