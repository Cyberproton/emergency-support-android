package hcmut.team15.emergencysupport.login;

public class LoginResponse {
    String message;
    private String accessToken;
    private String refreshToken;

    public String getMessage() {
        return message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
