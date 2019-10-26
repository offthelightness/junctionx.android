package com.passengers.juntionx.android.network.model

data class GetAtmResponseWithDistance(
    val total: Int,
    val items: List<AtmWithDistance>
)