package com.example.randomduckapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: DuckApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://random-d.uk/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DuckApi::class.java)
    }
}