package hcmut.team15.emergencysupport.profile;

public class Profile {
    String name;
    String phone;
    String address;
    String dateOfBirth;
    String bloodType;
    String allergens;
    boolean nameVisibility;
    boolean phoneVisibility;
    boolean addressVisibility;
    boolean dateOfBirthVisibility;
    boolean bloodTypeVisibility;
    boolean allergensVisibility;

    public Profile(String name, String phone, String address, String dateOfBirth, String bloodType, String allergens, boolean nameVisibility, boolean phoneVisibility, boolean addressVisibility, boolean dateOfBirthVisibility, boolean bloodTypeVisibility, boolean allergensVisibility) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.allergens = allergens;
        this.nameVisibility = nameVisibility;
        this.phoneVisibility = phoneVisibility;
        this.addressVisibility = addressVisibility;
        this.dateOfBirthVisibility = dateOfBirthVisibility;
        this.bloodTypeVisibility = bloodTypeVisibility;
        this.allergensVisibility = allergensVisibility;
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
}
