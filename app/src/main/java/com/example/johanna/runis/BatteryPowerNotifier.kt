package com.example.johanna.runis

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.os.BatteryManager



/**
 * Created by Edward on 10.10.2018.
 */
class BatteryPowerNotifier : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val curLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        if(curLevel == 99){
            Toast.makeText(context, "Battery is running low!", Toast.LENGTH_LONG).show()
        }
    }
}