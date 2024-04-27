package com.example.smartparent.data

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SmartGlassApi {
    @Multipart
    @POST("detect")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("select_mode") selectMode: RequestBody
    ): Observable<ArrayList<UploadResponse>>


    companion object {
        operator fun invoke(): SmartGlassApi {
            return Retrofit.Builder()
                .baseUrl("http://192.168.174.75:5001/")

                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(SmartGlassApi::class.java)
        }
    }
}