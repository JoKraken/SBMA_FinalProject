package com.example.johanna.runis

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_rundetails.*

class FragmentRunDetails: Fragment() {

    internal var activityCallBack: FragmentRunDetailsListener? = null

    interface FragmentRunDetailsListener {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_rundetails, container, false)
        //activityCallBack = context as FragmentRunDetailsListener

        if (getArguments() != null) {
            var arg = getArguments()!!.getStringArray("details")
            Log.d("DEBUG_details", arg[0]+", "+arg[1]+", "+arg[2])
            val date = rootView.findViewById<TextView>(R.id.date) as TextView
            date.text = ""+arg[0]
            val km = rootView.findViewById<TextView>(R.id.km) as TextView
            km.text = "km: "+arg[1]
            val time = rootView.findViewById<TextView>(R.id.time) as TextView
            time.text = "time: "+arg[2]
        }else{
            Log.d("DEBUG_details", "getArguments() == null")
        }

        return rootView
    }
}
