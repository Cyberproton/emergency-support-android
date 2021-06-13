package hcmut.team15.emergencysupport.model;

import com.google.gson.annotations.SerializedName;

public class HelpResponse {
    @SerializedName("case")
    private Case cs;

    private String message;
    private String error;

    public HelpResponse(Case cs, String message, String error) {
        this.cs = cs;
        this.message = message;
        this.error = error;
    }

    public Case getCase() {
        return cs;
    }

    public void setCase(Case cs) {
        this.cs = cs;
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
