package com.example.myapplication.network

import com.example.myapplication.data.Root
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

// Retrofit interface for authentication
interface AuthApiService {
    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun getToken(
        @Header("Authorization") authHeader: String,
        @Header("Content-Type") contentType: String,
        @Field("grant_type") grantType: String,
        @Field("scope") scope: String
    ): Call<Root>
}
