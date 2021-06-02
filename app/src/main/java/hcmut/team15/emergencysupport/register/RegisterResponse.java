package hcmut.team15.emergencysupport.register;

import hcmut.team15.emergencysupport.User;

public class RegisterResponse {
    private String message;
    private String error;
    private User user;

    public RegisterResponse(String message, String error, User user) {
        this.message = message;
        this.error = error;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public User getUser() {
        return user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setUser(User user) {
        this.user = user;
    }
}