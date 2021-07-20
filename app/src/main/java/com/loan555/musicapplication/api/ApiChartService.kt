package com.loan555.musicapplication.api

import com.google.gson.GsonBuilder
import com.loan555.musicapplication.model.DataChartResult
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiChartService {

    //http://mp3.zing.vn/xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1
    @GET("xhr/chart-realtime")
    fun getCurrentData(
        @Query("songId") songId: Int,
        @Query("videoId") videoId: Int,
        @Query("albumId") albumId: Int,
        @Query("chart") chart: String,
        @Query("time") time: Int
    ): Call<DataChartResult>

    companion object {
        private const val BaseUrl = "http://mp3.zing.vn/"
        fun create(): ApiChartService {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit.create(ApiChartService::class.java)
        }
    }
}
