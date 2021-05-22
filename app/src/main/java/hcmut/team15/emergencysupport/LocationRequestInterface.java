package hcmut.team15.emergencysupport;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LocationRequestInterface {
    @POST("/api/location")
    Call<Void> updateMyLocation(@Body Map<String, String> body, @Header("x-access-token") String token);
}
