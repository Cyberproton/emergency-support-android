package hcmut.team15.emergencysupport.login;


import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LoginInterface {
    @POST("/api/register")
    Call<Void> executeRegister(@Body Map<String, String> body);

    @POST("/api/login")
    Call<LoginResponse> executeLogin(@Body Map<String, String> body);

}