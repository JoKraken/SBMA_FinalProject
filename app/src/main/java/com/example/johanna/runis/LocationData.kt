package com.example.johanna.runis

import org.osmdroid.util.GeoPoint

/**
 * Created by Edward on 10.10.2018.
 */
class LocationData(geoPoint: GeoPoint, timeStamp: Long, speed: Float) {
    var geoPoint = geoPoint
    var timeStamp = timeStamp
    var speed = speed
}