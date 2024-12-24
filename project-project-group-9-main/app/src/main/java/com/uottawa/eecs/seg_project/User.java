package com.uottawa.eecs.seg_project;

public abstract class User{
    private String firstName;
    private String lastName;
    protected String email; //username also
    protected String password;
    private String phoneNumber;
    private String address;

    public User() {
    }
    public User(String firstName, String lastName, String email, String password, String phoneNumber, String address){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    // Abstract methods for login and logoff functionality
    public abstract boolean logIn(String email, String password);
    public abstract void logOff();
    //abstract method to registerUser\
}