package hcmut.team15.emergencysupport.login;

import com.google.gson.annotations.SerializedName;

public class LoginAnonymouslyResponse {
    @SerializedName("user")
    private String username;
    private String password;
    private String accessToken;
    private String refreshToken;
    private String message;
    private String error;

    public LoginAnonymouslyResponse(String username, String password, String accessToken, String refreshToken, String message, String error) {
        this.username = username;
        this.password = password;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
        this.error = error;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUser(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
