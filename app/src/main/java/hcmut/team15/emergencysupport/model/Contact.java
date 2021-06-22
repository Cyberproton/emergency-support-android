package hcmut.team15.emergencysupport.model;

public class Contact {
    private String name;
    private String phone;
    private String _id;
    private boolean expanded;

    public Contact(String name, String id, String phone) {
        this.name = name;
        this.phone = phone;
        this._id = id;
        this.expanded = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void set_id(String _id) {this._id = _id;}
    public String get_id() {
        return _id;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }
}
