@file:Suppress("DEPRECATION")

package com.example.johanna.runis

import android.app.ListFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_myruns.*

@Suppress("UNREACHABLE_CODE", "DEPRECATION")
class FragmentMyRuns: ListFragment() {
    internal var activityCallBack: FragmentMyRunsListener? = null

    interface FragmentMyRunsListener {
        fun onSwipeRightMyRuns()
        fun onSwipeLeftMyRuns()
        fun onListClick(position: Int)
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
    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        activityCallBack!!.onListClick(position)
    }

}