package com.passengers.juntionx.android.network.model


data class ATM(
    val id: String,
    val city: String,
    val zipCD: String,
    val address: String,
    val geoX: Double,
    val geoY: Double,
    val canDeposit: Boolean
)