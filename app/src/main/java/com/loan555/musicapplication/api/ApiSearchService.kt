package com.loan555.musicapplication.api

import com.google.gson.GsonBuilder
import com.loan555.musicapplication.model.DataSearchResult
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiSearchService {
    //http://ac.mp3.zing.vn/complete?type=artist,song,key,code&num=500&query=Anh Thế Giới Và Em
    @GET("complete")
    fun getCurrentData(
        @Query("type") type: String,
        @Query("num") num: Long,
        @Query("query") query: String
    ): Call<DataSearchResult>

    companion object {
        private const val BaseUrl = "http://ac.mp3.zing.vn/"
        fun create(): ApiSearchService {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit.create(ApiSearchService::class.java)
        }
    }
}