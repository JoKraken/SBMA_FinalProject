package com.example.johanna.runis.MyRuns

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.johanna.runis.R
import kotlinx.android.synthetic.main.fragment_prediction.*

@SuppressLint("SetTextI18n")
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FragmentPrediction: Fragment() {

    internal var km : Double = 0.0
    private var minutes : Long = 0L

    /*
    input: inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    output: View?
    description: create view, setOnClickListener to delete a run
    */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_prediction, container, false)

        val buttonKm = rootView.findViewById<Button>(R.id.button_km)
        val buttonMin = rootView.findViewById<Button>(R.id.button_minutes)
        buttonKm.setOnClickListener {
            Log.d("DEBUG_prediction", "buttonKm")
            predictKm()
        }
        buttonMin.setOnClickListener {
            Log.d("DEBUG_prediction", "buttonMin")
            predictMin()
        }

        if (arguments != null) {
            km = arguments!!.getDouble("km")
            minutes = arguments!!.getLong("minutes")
            Log.d("DEBUG_details", "km: "+km+", min: "+minutes)
        }else{
            Log.d("DEBUG_details", "getArguments() == null")
        }

        return rootView
    }

/*
    input: -
    output: void
    description: predict minutes
    */
    fun predictMin(){
        val temp = km /minutes
        val min = editText.text.toString().toDouble()
        if(km != 0.0 && minutes != 0L){
            textView.text = this.getString(R.string.prediction_return)+" "+min+" "+
                    this.getString(R.string.prediction_return_minutes)+" "+temp*min+" "+
                    this.getString(R.string.prediction_return_km)
            Log.d("DEBUG_details","You will run in "+min+" minutes "+ temp+" km")
        }else{
            textView.text = this.getString(R.string.prediction_canNot_km)
            Log.d("DEBUG_details","an error has currend")
        }

    }

    /*
    input: -
    output: void
    description: predict km
    */
    fun predictKm(){
        val temp = minutes / km
        val Kilometer= editText2.text.toString().toDouble()
        if(km  != 0.1 && minutes != 0L){
            textView2.text = this.getString(R.string.prediction_return)+" "+Kilometer+" "+
                    this.getString(R.string.prediction_return_km)+" "+temp*Kilometer+" "+
                    this.getString(R.string.prediction_return_minutes)
            Log.d("DEBUG_details","You will run in "+Kilometer+" minutes "+ temp+" km")
        }else{
            textView2.text = this.getString(R.string.prediction_canNot_minutes)
            Log.d("DEBUG_details","an error has currend")
        }
    }

}
