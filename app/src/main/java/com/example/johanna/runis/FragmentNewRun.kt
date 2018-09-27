package com.example.johanna.runis

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.FloatingActionButton
import android.os.SystemClock
import android.widget.Chronometer



class  FragmentNewRun: Fragment() {
    internal var activityCallBack: FragmentNewRunListener? = null
    private var chronometer: Chronometer? = null
    private var running: Boolean = false

    interface FragmentNewRunListener {
        fun onSwipeLeftNewRun(base: Long)
        fun endRun(time: Long)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_newrun, container, false)
        activityCallBack = context as FragmentNewRunListener

        rootView.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Home Left")
                activityCallBack!!.onSwipeLeftNewRun(chronometer!!.base)
                pauseChronometer()
            }
        })

        val check = rootView.findViewById(R.id.check) as FloatingActionButton
        check.setOnClickListener {
            pauseChronometer()
            activityCallBack!!.endRun(chronometer!!.base)
        }

        var time = 0
        if (getArguments() != null) {
            time = getArguments()!!.getLong("timer").toInt()
            Log.d("DEBUG", time.toString())
        }

        chronometer = rootView.findViewById(R.id.chronometer)
        chronometer!!.setFormat("Time: %s")
        chronometer!!.base = (SystemClock.elapsedRealtime() - (time))

        chronometer!!.setOnChronometerTickListener(
                Chronometer.OnChronometerTickListener { chronometer ->
//            if (SystemClock.elapsedRealtime() - chronometer.base >= 10000) {
//                chronometer.base = SystemClock.elapsedRealtime()
//                Toast.makeText(this.context, "Bing!", Toast.LENGTH_SHORT).show()
//            }
        })

        startChronometer()

        return rootView
    }

    fun startChronometer() {
        if (!running) {
            chronometer!!.start()
            running = true
        }
    }

    fun pauseChronometer() {
        if (running) {
            chronometer!!.stop()
            running = false
        }
    }
}