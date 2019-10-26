package com.passengers.juntionx.android.utils

import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import kotlin.math.cos
import kotlin.math.sin


fun LatLng.createSearchArea(radiusInMeters: Int): Pair<LatLng, LatLng> {

    val earth_radius = 6378137

    val delta_lat: Double = (
            radiusInMeters / earth_radius) * (180 / Math.PI)
    val delta_lon: Double =
        (radiusInMeters / earth_radius) * (180 / Math.PI) / cos(latitude * Math.PI / 180)

    return Pair(LatLng(latitude + delta_lat, longitude + delta_lon), LatLng(latitude - delta_lat, latitude - delta_lon))
}

fun LatLng.toSimpleString(): String {
    return this.latitude.toString() + ", " + this.longitude.toString()
}