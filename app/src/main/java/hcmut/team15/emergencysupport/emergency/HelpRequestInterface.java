package hcmut.team15.emergencysupport.emergency;

import java.util.List;
import java.util.Map;

import hcmut.team15.emergencysupport.model.Case;
import hcmut.team15.emergencysupport.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface HelpRequestInterface {
    @POST("/api/help/start")
    Call<Case> requestHelp(@Body Map<String, String> body, @Header("x-access-token") String token);

    @POST("/api/help/find")
    Call<List<User>> requestVolunteers(@Body Map<String, String> body);

    @POST("/api/help/stop")
    Call<Void> stopHelp(@Body Map<String, String> body, @Header("x-access-token") String token);

    @POST("/api/help/accept")
    Call<Void> acceptHelp(@Body Map<String, String> body);
}
