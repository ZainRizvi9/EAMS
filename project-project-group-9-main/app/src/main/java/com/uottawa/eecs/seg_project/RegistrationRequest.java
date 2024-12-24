package com.uottawa.eecs.seg_project;

// This class is used to handle the info of the registration requests
public class RegistrationRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String address;
    private String organization;
    private String password;
    private String phone;

    public RegistrationRequest(String firstName, String lastName, String email, String role, String address, String organization, String password, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.address = address;
        this.organization = organization;
        this.password = password;
        this.phone = phone;
    }

    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public String getEmail(){
        return email;
    }
    public String getRole(){
        return role;
    }

    public String getAddress() {
        return address;
    }

    public String getOrganization() {
        return organization;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " - " + role;
    }
}