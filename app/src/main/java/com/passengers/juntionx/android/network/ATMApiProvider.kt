package com.passengers.juntionx.android.network

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ATMApiProvider {

    companion object {
        val api: ATMApi = Retrofit.Builder()
            .baseUrl("http://100.98.11.218:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create<ATMApi>(ATMApi::class.java)

    }
}