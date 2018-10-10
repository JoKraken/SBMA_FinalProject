package com.example.johanna.runis.Home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.FloatingActionButton
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Chronometer
import android.widget.TextView
import com.example.johanna.runis.LocationData
import com.example.johanna.runis.OnSwipeTouchListener
import com.example.johanna.runis.R
import com.example.johanna.runis.RunRoute
import kotlinx.android.synthetic.main.fragment_newrun.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.concurrent.TimeUnit


class FragmentNewRun: Fragment(), LocationListener, SensorEventListener {

    internal var activityCallBack: FragmentNewRunListener? = null
    private var chronometer: Chronometer? = null
    private var running: Boolean = false
    private var waypoints = ArrayList<GeoPoint>()
    private var lengthKm = 0.0
    private var locationSteps = ArrayList<LocationData>()
    private lateinit var sm: SensorManager
    private var sTemp: Sensor? = null

    interface FragmentNewRunListener {
        fun onSwipeLeftNewRun()
        fun endRun(time: Long, runRoute: RunRoute, length: Double)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_newrun, container, false)
        sm = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sTemp = sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        activityCallBack = context as FragmentNewRunListener
        if (sTemp == null) {
            val temptxt = rootView.findViewById<TextView>(R.id.temperatureTextView)
            temptxt.setText(R.string.cannot_use_temperature)
        }
        rootView.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Home Left")
                activityCallBack!!.onSwipeLeftNewRun()
                pauseChronometer()
            }
        })

        val check = rootView.findViewById(R.id.check) as FloatingActionButton
        check.setOnClickListener {
            pauseChronometer()
            activityCallBack!!.endRun(chronometer!!.base, RunRoute(locationSteps, waypoints), lengthKm)
        }

        var time = 0
        if (arguments != null) {
            Log.d("DEBUG_newRun", arguments!!.getLong("timer").toString())
            time = arguments!!.getLong("timer").toInt()
            Log.d("DEBUG_newRun", time.toString())
        }

        chronometer = rootView.findViewById(R.id.chronometer)
        chronometer!!.format = "Time: %s"
        chronometer!!.base = (SystemClock.elapsedRealtime() - (time))

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        if ((Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this.context!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        || ContextCompat.checkSelfPermission(this.context!!,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
            this.requestPermissions()
        }
        else {
            startChronometer(rootView)
        }
        return rootView
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this.context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity!!,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this.activity!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        0)
            }
        } else {
        }
        if (ContextCompat.checkSelfPermission(this.context!!,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity!!,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this.activity!!,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1)
            }
        } else {
        }
    }

    private fun startChronometer(view: View) {
        val lm = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try{
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 1000, 50f, this)
        }
        catch (e: SecurityException){
            Log.d("error", e.message)
            return
        }
        val ctx = this.context
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        val map = view.findViewById(R.id.map) as MapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        map.controller.setZoom(20.0)
        if (!running) {
            chronometer!!.start()
            running = true
        }
    }

    private fun startChronometerAfterPermissionsRequest() {
        val lm = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try{
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 1000, 50f, this)
        }
        catch (e: SecurityException){
            Log.d("error", e.message)
            return
        }
        val ctx = this.context
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        val map = this.view!!.findViewById(R.id.map) as MapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        map.controller.setZoom(20.0)
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

    override fun onLocationChanged(p0: Location?) {	//new location react...
        Log.d("GEOLOCATION", "new latitude: ${p0?.latitude} and longitude: ${p0?.longitude}")
        if(p0 != null && map != null){
            map.controller.setCenter(GeoPoint(p0.latitude, p0.longitude))
            map.overlays.clear()
            val startMarker = Marker(map)
            startMarker.position = GeoPoint(p0)
            waypoints.add(GeoPoint(p0))
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            val roadManager = OSRMRoadManager(this.context)
            roadManager.addRequestOption("routeType=multimodal")
            val road = roadManager.getRoad(waypoints)
            if(waypoints.size >= 2){
                lengthKm += waypoints[waypoints.size-1].distanceToAsDouble(waypoints[waypoints.size-2])/1000
                lengthKm = Math.round(lengthKm * 100.0) / 100.0
                val location = LocationData(GeoPoint(p0), SystemClock.elapsedRealtime()-chronometer!!.base, waypoints[waypoints.size-1].distanceToAsDouble(waypoints[waypoints.size-2])/1000 / TimeUnit.MILLISECONDS.toSeconds(SystemClock.elapsedRealtime()-chronometer!!.base-locationSteps.get(locationSteps.size-1).timeStamp))
                locationSteps.add(location)

            }else{
                val location = LocationData(GeoPoint(p0), SystemClock.elapsedRealtime()-chronometer!!.base, 0.0)
                locationSteps.add(location)
            }
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay.color = Color.RED
            map.overlays.add(roadOverlay)
            map.overlays.add(startMarker)
            map.invalidate()
        }

    }
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
    override fun onProviderEnabled(p0: String?) {}
    override fun onProviderDisabled(p0: String?) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startChronometerAfterPermissionsRequest()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startChronometerAfterPermissionsRequest()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Not implemented
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor == sTemp && event?.values?.get(index = 0) != null){

            temperatureTextView.text = event.values[0].toString() + "°C"
        }
    }

    override fun onResume() {
        super.onResume()
        sTemp?.also {
            sm.registerListener(this, it,
                    SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sm.unregisterListener(this)
    }
}