package com.example.johanna.runis.MyRuns

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ListFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.example.johanna.runis.OnSwipeTouchListener
import com.example.johanna.runis.R

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FragmentMyRuns: ListFragment() {
    internal var activityCallBack: FragmentMyRunsListener? = null

    interface FragmentMyRunsListener {
        fun onSwipeRightMyRuns()
        fun onSwipeLeftMyRuns()
        fun onListClick(position: Int)
        fun newRun()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_myruns, container, false)
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

        val add = rootView.findViewById<FloatingActionButton>(R.id.add)
        add.setOnClickListener {
            activityCallBack!!.newRun()
        }

        if (arguments != null) {
            val totalKm = arguments!!.getDouble("totalKm")
            val totalKm_view = rootView.findViewById(R.id.totalKm) as TextView
            totalKm_view.text = totalKm.toString()+" "+getString(R.string.home_km)
            val totalTime_view = rootView.findViewById(R.id.totalTime) as TextView
            val totalTime = arguments!!.getString("totalTime")
            totalTime_view.text = totalTime+" "+getString(R.string.home_minutes)
        }

        return rootView
    }
    
    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        if(position!= 0){
            super.onListItemClick(l, v, position, id)
            activityCallBack!!.onListClick(position)
        }
    }

}