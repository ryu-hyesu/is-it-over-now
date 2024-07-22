package com.example.myapplication.network

import com.example.myapplication.data.Root
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TicketAPIService {
    @GET("discovery/v2/events.{type}")
    fun getDailyBoxOfficeResult(@Path("type") type : String,
                                @Query("apikey") apikey : String,
                                @Query("attractionId") attractionId : String,
                                @Query("size") size : String,
                                @Query("postalCode") postalCode : String,
                                @Query("id") id : String,
    ) : Call<Root>

}