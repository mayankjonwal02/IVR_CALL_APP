package com.example.ivr_calling_app.android

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiService {

    @GET("testconnection.php")
    suspend fun getconnection(): ResponseBody

    @GET("read.php")
    suspend fun getdata(): List<patientdata>

    @GET("read.php")
    suspend fun getdatabyid(@Query("calledon") calledon: String?): List<patientdata>

    @FormUrlEncoded
    @POST("create.php")
    suspend fun senddata(@Field("engagementid") engagementid: String,
                         @Field("patientid") patientid: String,
                         @Field("patientname") patientname: String,
                         @Field("patientcno") patientcno: String,
                         @Field("operationtype") operationtype: String,
                         @Field("language") language: String,
                         @Field("duedate") duedate: String,
                         @Field("calledon") calledon: String,
                         @Field("day7") day7: String,
                         @Field("day6") day6: String,
                         @Field("day5") day5: String,
                         @Field("day4") day4: String,
                         @Field("day3") day3: String,
                         @Field("day2") day2: String,
                         @Field("day1") day1: String) : ResponseBody

    @DELETE("delete.php")
    suspend fun deletedata(@Query("engagementid") engagementid: String) : ResponseBody




    @PUT("update.php")
    fun updatefield(
        @Query("field") fieldToUpdate: String?,
        @Body data: JsonObject?
    ): Call<ResponseBody?>?

    @GET("getdb.php")
    suspend fun getalldbs() : List<String>

}