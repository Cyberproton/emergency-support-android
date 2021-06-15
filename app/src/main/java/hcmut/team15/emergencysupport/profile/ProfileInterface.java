package hcmut.team15.emergencysupport.profile;

import java.util.Map;

import hcmut.team15.emergencysupport.login.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ProfileInterface {
    @POST("/api/login")
    Call<ProfileResponse> executeProfile(@Body Map<String, String> body);
}
