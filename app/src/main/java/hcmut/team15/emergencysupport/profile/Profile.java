package hcmut.team15.emergencysupport.profile;

public class Profile {
    String name;
    String phone;
    String address;
    String allergy;

    public Profile(String name, String phone, String address, String allergy, String dateOfBirth, String bloodType) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.allergy = allergy;
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

    public String getAllergy() {
        return allergy;
    }

    public void setAllergy(String allergy) {
        this.allergy = allergy;
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
