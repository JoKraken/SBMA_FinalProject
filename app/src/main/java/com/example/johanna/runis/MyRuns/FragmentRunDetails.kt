package com.example.johanna.runis.MyRuns

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.johanna.runis.R
import com.example.johanna.runis.RunRoute
import com.google.gson.Gson
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_newrun.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FragmentRunDetails: Fragment() {

    private var activityCallBack: FragmentRunDetailsListener? = null
    private var runID: Int = 0
    private var runRoute: RunRoute? = null

    interface FragmentRunDetailsListener {
        fun deleteRun(id: Int)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_rundetails, container, false)
        activityCallBack = context as FragmentRunDetailsListener


        val delete = rootView.findViewById<FloatingActionButton>(R.id.delete)
        delete.setOnClickListener {
            Log.d("DEBUG_runDetails", "in onclick")
            activityCallBack!!.deleteRun(runID)
        }
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        if (arguments != null) {
            val arg = arguments!!.getStringArray("details")
            //Log.d("DEBUG_details", arg[0]+", "+arg[1]+", "+arg[2])
            val date = rootView.findViewById<TextView>(R.id.date)
            date.text = ""+arg[0]
            val km = rootView.findViewById<TextView>(R.id.km)
            km.text = "km: "+arg[1]
            val time = rootView.findViewById<TextView>(R.id.time)
            time.text = "time: "+arg[2]
            runID = arg[3].toInt()
            runRoute = Gson().fromJson(arg[4], RunRoute::class.java)
            val mapView = rootView.findViewById<MapView>(R.id.map)
            if ((Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this.context!!,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                this.requestPermissions()
            }
            else {
                this.initializeMap(mapView)
            }
            val datapoints = Array(runRoute!!.waypoints.size) {
                DataPoint(TimeUnit.MILLISECONDS.toSeconds(runRoute!!.waypoints[it].timeStamp).toDouble(), runRoute!!.waypoints[it].speed.toDouble())
            }

            val graphView = rootView.findViewById<GraphView>(R.id.graph)
            graphView.addSeries(LineGraphSeries<DataPoint>(datapoints))
            graphView.gridLabelRenderer.verticalAxisTitle = "Speed"
            graphView.gridLabelRenderer.numVerticalLabels = 5
            val nf = NumberFormat.getInstance()
            nf.maximumFractionDigits = 0
            graphView.gridLabelRenderer.labelFormatter = DefaultLabelFormatter(nf, nf)
            graphView.viewport.isYAxisBoundsManual = true
            graphView.viewport.setMinY(0.0)
            graphView.viewport.setMaxY(20.0)
            if(runRoute!!.waypoints.size > 1){
                graphView.viewport.isXAxisBoundsManual = true
                graphView.viewport.setMinX(0.0)
                graphView.viewport.setMaxX(TimeUnit.MILLISECONDS.toSeconds(runRoute!!.waypoints[runRoute!!.waypoints.size-1].timeStamp).toDouble())
            }
        }else{
            Log.d("DEBUG_details", "getArguments() == null")
        }

        return rootView
    }

    private fun initializeMap(mv: MapView){
        mv.setTileSource(TileSourceFactory.MAPNIK)
        mv.setBuiltInZoomControls(true)
        mv.setMultiTouchControls(true)
        mv.controller.setZoom(15.0)
        if(runRoute!!.geopoints.size > 0){
            mv.controller.setCenter(runRoute!!.geopoints[0])
            val roadManager = OSRMRoadManager(this.context)
            roadManager.addRequestOption("routeType=multimodal")
            val road = roadManager.getRoad(runRoute!!.geopoints)
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay.color = Color.RED

            mv.overlays.add(roadOverlay)
            mv.invalidate()
        }
        else{
            mv.controller.setCenter(GeoPoint(0.0, 0.0))
        }

    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this.context!!,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity!!,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this.activity!!,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        0)
            }
        } else {
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initializeMap(map)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

        }
    }

}
