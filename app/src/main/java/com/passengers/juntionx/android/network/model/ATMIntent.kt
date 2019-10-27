package com.passengers.juntionx.android.network.model

import java.time.LocalDateTime

data class ATMIntent(
    val userId: String = "",
    val atmId: String = "",
    val expiredTime: LocalDateTime? = null,
    val realDistanceToAtmInMeters: Double = 0.0,
    val averageHistoricalWaitingTime: Double = 0.0,
    val realtimeWaitingTime: Double = 0.0

)