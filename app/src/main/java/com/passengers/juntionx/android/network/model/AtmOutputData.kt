package com.passengers.juntionx.android.network.model

data class AtmOutputData(
    val atm: ATM,
    val loadLevel: LoadLevel,
    val distanceInMeters: Double?
)