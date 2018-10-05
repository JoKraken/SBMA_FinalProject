package com.example.johanna.runis.Home

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.FloatingActionButton
import android.widget.TextView
import com.example.johanna.runis.OnSwipeTouchListener
import com.example.johanna.runis.R


class FragmentHome : Fragment() {
    internal var activityCallBack: FragmentHomeListener? = null

    interface FragmentHomeListener {
        fun onSwipeLeftHome()
        fun newRun()
        fun connectBT()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView : View? = inflater!!.inflate(R.layout.fragment_home, container, false)
        activityCallBack = context as FragmentHomeListener

        rootView!!.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Home Left")
                activityCallBack!!.onSwipeLeftHome()
            }
        })

        val add = rootView.findViewById<FloatingActionButton>(R.id.add)
        val bt = rootView.findViewById<FloatingActionButton>(R.id.startBlueTooth)
        add.setOnClickListener {
            activityCallBack!!.newRun()
        }
        bt.setOnClickListener {
            activityCallBack!!.connectBT()
        }

        if(getArguments() != null){
            val args = getArguments()!!.getStringArray("details")
            val last_date = rootView.findViewById<TextView>(R.id.last_date) as TextView
            last_date.text = args[0]
            val last_km = rootView.findViewById<TextView>(R.id.last_km2) as TextView
            last_km.text = args[1]+" "+getString(R.string.home_km)
            val last_time = rootView.findViewById<TextView>(R.id.last_time2) as TextView
            last_time.text = args[2]+" "+getString(R.string.home_minutes)
            val headline = rootView.findViewById<TextView>(R.id.txtheadline) as TextView
            headline.text = headline.text.toString()+" "+args[3]+","
        }

        return rootView
    }

}