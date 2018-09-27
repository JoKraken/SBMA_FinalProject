package com.example.johanna.runis

import android.Manifest
import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.FloatingActionButton
import com.polidea.rxandroidble.RxBleDevice
import java.nio.file.Files.size
import android.widget.AdapterView
import kotlinx.android.synthetic.main.fragment_bluetooth.view.*
import rx.Subscription
import android.Manifest.permission
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.polidea.rxandroidble.RxBleClient
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.polidea.rxandroidble.scan.ScanSettings
import kotlinx.android.synthetic.main.fragment_bluetooth.*


import com.movesense.mds.Mds;
import com.movesense.mds.MdsConnectionListener
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsResponseListener;

class BluetoothFragment : Fragment(), AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    private val LOG_TAG = MainActivity::class.java.simpleName
    private val MY_PERMISSIONS_REQUEST_LOCATION = 1

    // MDS
    private var mMds: Mds? = null
    val URI_CONNECTEDDEVICES = "suunto://MDS/ConnectedDevices"
    val URI_EVENTLISTENER = "suunto://MDS/EventListener"
    val SCHEME_PREFIX = "suunto://"

    // BleClient singleton
    private var mBleClient: RxBleClient? = null

    // UI
    private var mScanResultListView: ListView? = null
    private val mScanResArrayList = java.util.ArrayList<MyScanResult>()
    private var mScanResArrayAdapter: ArrayAdapter<MyScanResult>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_bluetooth, container, false)

        // Init Scan UI

        // Make sure we have all the permissions this app needs
        requestNeededPermissions()

        // Initialize Movesense MDS library
        initMds()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mScanResArrayAdapter = ArrayAdapter(this.context,
                android.R.layout.simple_list_item_1, mScanResArrayList)
        listScanResult.setOnItemLongClickListener(this)
        listScanResult.setOnItemClickListener(this)
        listScanResult.setAdapter(mScanResArrayAdapter)
        buttonScan.setOnClickListener {
            onScanClicked()
        }
        buttonScanStop.setOnClickListener {
            onScanStopClicked()
        }
    }


    private fun getBleClient(): RxBleClient {
        // Init RxAndroidBle (Ble helper library) if not yet initialized
        if (mBleClient == null) {
            mBleClient = RxBleClient.create(this.context!!)
        }

        return mBleClient!!
    }

    private fun initMds() {
        mMds = Mds.builder().build(this.context)
    }

    fun requestNeededPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this.context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this.activity!!,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION)

        }
    }

    var mScanSubscription: Subscription? = null

    fun onScanClicked() {
        buttonScan.setVisibility(View.GONE)
        buttonScanStop.setVisibility(View.VISIBLE)

        // Start with empty list
        mScanResArrayList.clear()
        mScanResArrayAdapter!!.notifyDataSetChanged()

        mScanSubscription = getBleClient().scanBleDevices(
                ScanSettings.Builder()
                        // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
                // add filters if needed
        )
                .subscribe(
                        { scanResult ->
                            Log.d(LOG_TAG, "scanResult: " + scanResult)

                            // Process scan result here. filter movesense devices.
                            if (scanResult.getBleDevice() != null &&
                                    scanResult.getBleDevice().getName() != null &&
                                    scanResult.getBleDevice().getName()!!.startsWith("Movesense")) {

                                // replace if exists already, add otherwise
                                val msr = MyScanResult(scanResult)
                                if (mScanResArrayList.contains(msr))
                                    mScanResArrayList.set(mScanResArrayList.indexOf(msr), msr)
                                else
                                    mScanResArrayList.add(0, msr)

                                mScanResArrayAdapter!!.notifyDataSetChanged()
                            }
                        },
                        { throwable ->
                            Log.e(LOG_TAG, "scan error: " + throwable)
                            // Handle an error here.

                            // Re-enable scan buttons, just like with ScanStop
                            onScanStopClicked()
                        }
                )
    }

    fun onScanStopClicked() {
        if (mScanSubscription != null) {
            mScanSubscription!!.unsubscribe()
            mScanSubscription = null
        }

        buttonScan.setVisibility(View.VISIBLE)
        buttonScanStop.setVisibility(View.GONE)
    }

    fun showDeviceInfo(serial: String) {
        val uri = SCHEME_PREFIX + serial + "/Info"
        val ctx = this
        mMds!!.get(uri, null, object : MdsResponseListener {
            override fun onSuccess(s: String) {
                Log.i(LOG_TAG, "Device $serial /info request succesful: $s")
                // Display info in alert dialog
                val builder = AlertDialog.Builder(ctx.context!!)
                builder.setTitle("Device info:")
                        .setMessage(s)
                        .show()
            }

            override fun onError(e: MdsException) {
                Log.e(LOG_TAG, "Device $serial /info returned error: $e")
            }
        })
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (position < 0 || position >= mScanResArrayList.size)
            return

        val device = mScanResArrayList.get(position)
        if (!device.isConnected) {
            // Stop scanning
            onScanStopClicked()

            // And connect to the device
            connectBLEDevice(device)
        } else {
            // Device is connected, trigger showing /Info
            showDeviceInfo(device.connectedSerial!!)
        }
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        if (position < 0 || position >= mScanResArrayList.size)
            return false

        val device = mScanResArrayList.get(position)

        Log.i(LOG_TAG, "Disconnecting from BLE device: " + device.macAddress)
        mMds!!.disconnect(device.macAddress)

        return true
    }

    private fun connectBLEDevice(device: MyScanResult) {
        val bleDevice = getBleClient().getBleDevice(device.macAddress)

        Log.i(LOG_TAG, "Connecting to BLE device: " + bleDevice.macAddress)
        mMds!!.connect(bleDevice.macAddress, object : MdsConnectionListener {

            override fun onConnect(s: String) {
                Log.d(LOG_TAG, "onConnect:" + s)
            }

            override fun onConnectionComplete(macAddress: String, serial: String) {
                for (sr in mScanResArrayList) {
                    if (sr.macAddress.toLowerCase().equals(macAddress.toLowerCase())) {
                        sr.markConnected(serial)
                        break
                    }
                }
                mScanResArrayAdapter!!.notifyDataSetChanged()
            }

            override fun onError(e: MdsException) {
                Log.e(LOG_TAG, "onError:" + e)

                showConnectionError(e)
            }

            override fun onDisconnect(bleAddress: String) {
                Log.d(LOG_TAG, "onDisconnect: " + bleAddress)
                for (sr in mScanResArrayList) {
                    if (bleAddress == sr.macAddress)
                        sr.markDisconnected()
                }
                mScanResArrayAdapter!!.notifyDataSetChanged()
            }
        })
    }

    private fun showConnectionError(e: MdsException) {
        val builder = AlertDialog.Builder(this.context!!)
                .setTitle("Connection Error:")
                .setMessage(e.message)

        builder.create().show()
    }

}
