package hcmut.team15.emergencysupport

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginInterface {
    @POST("/api/login")
    fun executeLogin(@Body body: Map<String, String>): Call<User>

    @POST("/api/register")
    fun executeRegister(@Body body: Map<String, String>): Call<Unit>
}