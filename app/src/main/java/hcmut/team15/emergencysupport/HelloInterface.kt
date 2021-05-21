package hcmut.team15.emergencysupport

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface HelloInterface {
    @POST("/api/hello")
    fun executePostHello(@Body body: Map<String, String>): Call<HelloResponse>

    @GET("/api/hello")
    fun executeGetHello(): Call<HelloResponse>
}