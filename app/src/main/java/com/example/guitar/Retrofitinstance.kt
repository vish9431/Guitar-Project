package com.example.guitar

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object Retrofitinstance {
    private const val BASE_URL = "http://13.53.33.219:5000/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // GsonConverterFactory for JSON responses
            .build()
    }

    val Apiinterface: Apiinterface by lazy {
        createApiService()
    }

    private fun createApiService(): Apiinterface {
        return try {
            retrofit.create(Apiinterface::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e  // Rethrow the exception to handle it at a higher level
        }
    }
}
