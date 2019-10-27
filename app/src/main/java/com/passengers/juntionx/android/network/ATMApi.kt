package com.passengers.juntionx.android.network

import com.passengers.juntionx.android.network.model.ATMIntent
import com.passengers.juntionx.android.network.model.AtmSearchResult
import com.passengers.juntionx.android.network.model.GetAtmResponseWithDistance
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ATMApi {

    @GET("/atm/v2")
    fun getATMs(
        @Query("canDeposit") canDeposit: Boolean?,
        @Query("ne") northEast: String,
        @Query("sw") southWest: String
    ): Observable<GetAtmResponseWithDistance>

    @GET("/atm/v3")
    fun getATMsv3(
        @Query("canDeposit") canDeposit: Boolean?,
        @Query("ne") northEast: String,
        @Query("sw") southWest: String,
        @Query("location") location: String?,
        @Query("usePrediction") usePrediction: Boolean?
    ): Observable<AtmSearchResult>

    @POST("atm/intent/v1")
    fun postATMIntent(
        @Body atmIntent: ATMIntent
    ): Completable

}