package hcmut.team15.emergencysupport.profile;

public class ProfileResponse {
    private Profile profile;
    private String error;

    public ProfileResponse(Profile profile, String error) {
        this.profile = profile;
        this.error = error;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
