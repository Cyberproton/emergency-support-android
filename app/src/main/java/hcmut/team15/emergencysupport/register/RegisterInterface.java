package hcmut.team15.emergencysupport.register;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegisterInterface {
    @POST("/api/register")
    Call<hcmut.team15.emergencysupport.register.RegisterResponse> register(@Body Map<String, String> body);
}