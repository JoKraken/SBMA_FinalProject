package com.example.johanna.runis

import com.polidea.rxandroidble.RxBleDevice
import com.polidea.rxandroidble.scan.ScanResult

internal class MyScanResult(scanResult: ScanResult) {
    var rssi: Int = 0
    var macAddress: String
    var name: String
    var connectedSerial: String? = null

    val isConnected: Boolean
        get() = connectedSerial != null

    init {
        this.macAddress = scanResult.getBleDevice().getMacAddress()
        this.rssi = scanResult.getRssi()
        if(scanResult.getBleDevice().getName() != null){
            this.name = scanResult.getBleDevice().getName()!!
        }
        else{
            this.name = "nameless device"
        }
    }

    fun markConnected(serial: String) {
        connectedSerial = serial
    }

    fun markDisconnected() {
        connectedSerial = null
    }

    override fun equals(`object`: Any?): Boolean {
        return if (`object` is MyScanResult && `object`.macAddress == this.macAddress) {
            true
        } else if (`object` is RxBleDevice && (`object` as RxBleDevice).getMacAddress().equals(this.macAddress)) {
            true
        } else {
            false
        }
    }

    override fun toString(): String {
        return (if (isConnected) "*** " else "") + macAddress + " - " + name + " [" + rssi + "]" + if (isConnected) " ***" else ""
    }
}