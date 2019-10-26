package com.passengers.juntionx.android.network

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ATMApi {

    @GET("/atm")
    fun getATMs(@Query("canDeposit") boolean: Boolean?,
                @Query("ne") northEast: String,
                @Query("sw") southWest: String
    ): Observable<Any>

}