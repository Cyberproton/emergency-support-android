package hcmut.team15.emergencysupport.profile;

import java.util.Map;

import hcmut.team15.emergencysupport.login.LoginResponse;
import hcmut.team15.emergencysupport.model.ContactsResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ProfileInterface {
    @GET("/api/profile")
    Call<ProfileResponse> getProfile(@Header("x-access-token") String token);

    @PUT("api/profile")
    Call<ProfileResponse> setProfile(@Header("x-access-token") String token, @Body Map<String, String> body);
}
