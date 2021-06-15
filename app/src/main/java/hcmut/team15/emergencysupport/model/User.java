package hcmut.team15.emergencysupport.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("_id")
    private String username;

    private String password;

    private Location currentLocation;

    public User(String username, String password, Location currentLocation) {
        this.username = username;
        this.password = password;
        this.currentLocation = currentLocation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
