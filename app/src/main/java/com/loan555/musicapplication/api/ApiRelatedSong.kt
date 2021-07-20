package com.loan555.musicapplication.api

import com.google.gson.GsonBuilder
import com.loan555.musicapplication.model.RelatedSong
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiRelatedSong {
    //http://mp3.zing.vn/xhr/recommend?type=audio&id=ZW67OIA0
    @GET("xhr/recommend?type=audio")
    fun getCurrentData(
        @Query("id") id: String
    ): Call<RelatedSong>

    companion object {
        private const val BaseUrl = "http://mp3.zing.vn/"
        fun create(): ApiRelatedSong {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit.create(ApiRelatedSong::class.java)
        }
    }
}