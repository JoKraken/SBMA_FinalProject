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
        fun newPrediction()
    }

    /*
    input: inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    output: View?
    description: create view, setOnTouchListener for swiping, setOnClickListener to start a new run
    */
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

        val predict = rootView.findViewById<FloatingActionButton>(R.id.predict)
        predict.setOnClickListener {
            activityCallBack!!.newPrediction()
        }

        if (arguments != null) {
            val totalKm = arguments!!.getDouble("totalKm")
            val totalKmView = rootView.findViewById(R.id.totalKm) as TextView
            totalKmView.text = totalKm.toString()+" "+getString(R.string.home_km)
            val totalTimeView = rootView.findViewById(R.id.totalTime) as TextView
            val totalTime = arguments!!.getString("totalTime")
            totalTimeView.text = totalTime+" "+getString(R.string.home_minutes)
        }

        return rootView
    }

    /*
    input: l: ListView?, v: View?, position: Int, id: Long
    output: void
    description: if you click on a item in the list and the position != 0 the function onListClick(position) on the interface is called
    */
    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        if(position!= 0){
            super.onListItemClick(l, v, position, id)
            activityCallBack!!.onListClick(position)
        }
    }

}