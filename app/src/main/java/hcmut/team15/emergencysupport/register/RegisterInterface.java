
package hcmut.team15.emergencysupport.register;

import java.util.Map;

import hcmut.team15.emergencysupport.model.RegisterResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegisterInterface {
    @POST("/api/register")
    Call<RegisterResponse> register(@Body Map<String, String> body);
}