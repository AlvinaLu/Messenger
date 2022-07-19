package com.android.example.messenger.ui.experimental


import com.android.example.messenger.data.vo.UserListVO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.util.concurrent.CompletableFuture

private const val BASE_URL = "https://messenger-lushnalv.herokuapp.com/"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
    .build()

interface AppWebApi {


    @GET("users")
    fun listUsers(@Header("Authorization") authorization: String): CompletableFuture<UserListVO>

    companion object {
        @Volatile
        private var INSTANCE: AppWebApi? = null

        fun getApiService(): AppWebApi {
            return INSTANCE ?: synchronized(this) {
                val instance = retrofit.create(AppWebApi::class.java)
                INSTANCE = instance
                instance
            }
        }
    }
}
