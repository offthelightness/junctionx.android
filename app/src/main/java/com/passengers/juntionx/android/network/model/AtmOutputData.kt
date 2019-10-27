package com.passengers.juntionx.android.network.model

data class AtmOutputData(
    val atm: ATM,
    val loadLevel: LoadLevel,
    val lineDistanceInMeters: Double?,
    val realDistanceInMeters: Double?,
    val averageHistoricalWaitingTime: Double?,
    val realtimeWaitingTime: Double?
)