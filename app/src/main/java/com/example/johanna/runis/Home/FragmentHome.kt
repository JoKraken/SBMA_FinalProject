package com.example.johanna.runis.Home

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.johanna.runis.OnSwipeTouchListener
import com.example.johanna.runis.R


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FragmentHome : Fragment() {
    internal var activityCallBack: FragmentHomeListener? = null

    interface FragmentHomeListener {
        fun onSwipeLeftHome()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView : View? = inflater.inflate(R.layout.fragment_home, container, false)
        activityCallBack = context as FragmentHomeListener

        rootView!!.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Home Left")
                activityCallBack!!.onSwipeLeftHome()
            }
        })


        if(arguments != null){
            val args = arguments!!.getStringArray("details")
            val date = rootView.findViewById(R.id.last_date) as TextView
            date.text = args[0]
            val km = rootView.findViewById(R.id.last_km2) as TextView
            km.text = args[1]+" "+getString(R.string.home_km)
            val time = rootView.findViewById(R.id.last_time2) as TextView
            time.text = args[2]+" "+getString(R.string.home_minutes)
            val headline = rootView.findViewById(R.id.txtheadline) as TextView
            headline.text = headline.text.toString()+" "+args[3]+","
        }

        return rootView
    }

}