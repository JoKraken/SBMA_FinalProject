package com.example.johanna.runis.Home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.example.johanna.runis.OnSwipeTouchListener
import com.example.johanna.runis.R
import kotlinx.android.synthetic.main.fragment_newrun.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.PathOverlay
import org.osmdroid.views.overlay.Polyline


class FragmentNewRun: Fragment(), LocationListener {
    internal var activityCallBack: FragmentNewRunListener? = null
    private var chronometer: Chronometer? = null
    private var running: Boolean = false
    private var waypoints = ArrayList<GeoPoint>();
    interface FragmentNewRunListener {
        fun onSwipeLeftNewRun()
        fun endRun(time: Long)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_newrun, container, false)
        activityCallBack = context as FragmentNewRunListener

        rootView.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Home Left")
                activityCallBack!!.onSwipeLeftNewRun()
                pauseChronometer()
            }
        })

        val check = rootView.findViewById<FloatingActionButton>(R.id.check) as FloatingActionButton
        check.setOnClickListener {
            pauseChronometer()
            activityCallBack!!.endRun(chronometer!!.base)
        }

        var time = 0
        if (getArguments() != null) {
            Log.d("DEBUG_newRun", getArguments()!!.getLong("timer").toString())
            time = getArguments()!!.getLong("timer").toInt()
            Log.d("DEBUG_newRun", time.toString())
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
        var policy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if ((Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this.context!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            this.requestPermissions();
        }
        else {
            startChronometer(rootView)
        }
        return rootView
    }

    fun requestPermissions() {
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

    }

    fun startChronometer(view: View) {
        val lm = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try{
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 1000, 50f, this)
        }
        catch (e: SecurityException){
            Log.d("error", e.message);
            return;
        }
        val ctx = this.context
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        val map = view.findViewById<MapView>(R.id.map) as MapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        map.controller.setZoom(20.0)
        if (!running) {
            chronometer!!.start()
            running = true
        }
    }

    fun startChronometerAfterPermissionsRequest() {
        val lm = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try{
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 1000, 50f, this)
        }
        catch (e: SecurityException){
            Log.d("error", e.message);
            return;
        }
        val ctx = this.context
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        val map = this.view!!.findViewById<MapView>(R.id.map) as MapView
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
            map.controller.setCenter(GeoPoint(p0!!.latitude, p0!!.longitude))
            map.overlays.clear()
            val startMarker = Marker(map)
            startMarker.position = GeoPoint(p0!!)
            waypoints.add(GeoPoint(p0!!));
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            var roadManager = OSRMRoadManager(this.context);
            roadManager.addRequestOption("routeType=multimodal");
            var road = roadManager.getRoad(waypoints);
            var roadOverlay = RoadManager.buildRoadOverlay(road);
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

        // Add other 'when' lines to check for other
        // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }

    }
}