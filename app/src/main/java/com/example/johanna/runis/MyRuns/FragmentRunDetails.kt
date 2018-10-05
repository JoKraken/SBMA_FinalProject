package com.example.johanna.runis.MyRuns

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.johanna.runis.R

class FragmentRunDetails: Fragment() {

    internal var activityCallBack: FragmentRunDetailsListener? = null
    private var runID: Int = 0

    interface FragmentRunDetailsListener {
        fun deleteRun(id: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_rundetails, container, false)
        activityCallBack = context as FragmentRunDetailsListener


        val delete = rootView.findViewById<FloatingActionButton>(R.id.delete)
        delete.setOnClickListener {
            Log.d("DEBUG_runDetails", "in onclick")
            activityCallBack!!.deleteRun(runID)
        }

        if (getArguments() != null) {
            var arg = getArguments()!!.getStringArray("details")
            //Log.d("DEBUG_details", arg[0]+", "+arg[1]+", "+arg[2])
            val date = rootView.findViewById<TextView>(R.id.date) as TextView
            date.text = ""+arg[0]
            val km = rootView.findViewById<TextView>(R.id.km) as TextView
            km.text = "km: "+arg[1]
            val time = rootView.findViewById<TextView>(R.id.time) as TextView
            time.text = "time: "+arg[2]
            runID = arg[3].toInt()
        }else{
            Log.d("DEBUG_details", "getArguments() == null")
        }

        return rootView
    }
}
