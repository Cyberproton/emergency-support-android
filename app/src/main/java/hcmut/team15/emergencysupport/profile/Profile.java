package hcmut.team15.emergencysupport.profile;

public class Profile {
    String name;
    String phone;
    String address;
    String allergens;

    public Profile(String name, String phone, String address, String allergens, String dateOfBirth, String bloodType) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.allergens = allergens;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    String dateOfBirth;
    String bloodType;

}
