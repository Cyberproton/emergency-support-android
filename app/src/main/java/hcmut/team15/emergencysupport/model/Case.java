package hcmut.team15.emergencysupport.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Case {
    @SerializedName("_id")
    @Expose
    private String id;

    @SerializedName("caller")
    @Expose
    private User caller;

    @SerializedName("volunteers")
    @Expose
    private List<User> volunteers;

    @SerializedName("is_closed")
    @Expose
    private boolean isClosed;

    @SerializedName("location")
    @Expose
    private String location;

    public Case(String id, User caller, List<User> volunteers, boolean isClosed, String location) {
        this.id = id;
        this.caller = caller;
        this.volunteers = volunteers;
        this.isClosed = isClosed;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getCaller() {
        return caller;
    }

    public void setCaller(User caller) {
        this.caller = caller;
    }

    public List<User> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(List<User> volunteers) {
        this.volunteers = volunteers;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
