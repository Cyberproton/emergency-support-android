package hcmut.team15.emergencysupport.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Case {
    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("caller")
    @Expose
    private String caller;

    public Case(String id, String caller) {
        this.id = id;
        this.caller = caller;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }
}
