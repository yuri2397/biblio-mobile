package com.univ.thies.bibliothque.models;

import com.google.gson.annotations.SerializedName;

public class User {

    private static final String TAG = "USER" ;
    private int id;
    private Integer status;

    @SerializedName("profile_image")
    private String profileImage;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    private String ufr;

    private String departement;

    private String email;
    private String number;

    public String getUfr() {
        return ufr;
    }

    public void setUfr(String ufr) {
        this.ufr = ufr;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }


    public User(){}

    public String getFirstName() {
        return firstName;
    }

    public Integer getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", status=" + status +
                ", profileImage='" + profileImage + '\'' +
                ", firsName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", ufr='" + ufr + '\'' +
                ", departement='" + departement + '\'' +
                ", email='" + email + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
