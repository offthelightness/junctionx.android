package com.passengers.juntionx.android.network.model

data class AtmSearchResult(
    val bestAtm: AtmOutputData?,
    val items: List<AtmOutputData>
)