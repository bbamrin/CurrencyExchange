package com.example.currencyv1.api

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL: String = "https://api.exchangeratesapi.io";

fun createForeignExchangeService (): ForeignExchangeService {
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    return  retrofit.create(ForeignExchangeService::class.java)
}

interface ForeignExchangeService {
    @GET("/latest")
    fun latestBaseCurrency (
        @Query("base")baseCurrencyName: String,
        @Query("symbols")pairsNames: String
    ) : Call<BaseCurrency>
}