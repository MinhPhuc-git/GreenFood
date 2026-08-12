package com.example.GreenFood.user;

public class AddressDTO {
    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;
    private Boolean isDefault;

    public AddressDTO() {}

    public AddressDTO(String street, String ward, String district, String city, String country, Boolean isDefault) {
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.country = country;
        this.isDefault = isDefault;
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
