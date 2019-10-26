package com.passengers.juntionx.android.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.sin


fun LatLng.createSearchArea(radiusInMeters: Int): Pair<LatLng, LatLng> {

    val toNorthEast = Math.toRadians(45.0)

    val n = longitude + 180 / Math.PI * (radiusInMeters / 6378137) / cos(0.0) * cos(toNorthEast)
    val e = latitude + 180 / Math.PI * (radiusInMeters / 6378137).toDouble() * sin(toNorthEast)

    val toSouthWest = Math.toRadians(-135.0)

    val s = longitude + 180 / Math.PI * (radiusInMeters / 6378137) / cos(180.0) * cos(toSouthWest)
    val w = latitude + 180 / Math.PI * (radiusInMeters / 6378137).toDouble() * sin(toSouthWest)

    return Pair(LatLng(n, e), LatLng(s, w))
}