package com.uottawa.eecs.seg_project;

import java.util.Map;
import java.util.HashMap;

public class Organizer extends User {
    private String organizationName;
    static Map<String, String> registeredOrganizers = new HashMap<>();
    private static boolean loggedIn = false;

    public Organizer(String firstName, String lastName, String email, String password, String phoneNumber, String address, String organizationName) {
        super(firstName, lastName, email, password, phoneNumber, address);
        this.organizationName = organizationName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public boolean registerUser() {
        if (registeredOrganizers.containsKey(getEmail())) {
            System.out.println("You are already registered with this email.");
            return false;
        }
        registeredOrganizers.put(getEmail(), getPassword());
        System.out.println("You have successfully registered.");
        return true;
    }

    public boolean logIn(String email, String password) {
        if (registeredOrganizers.containsKey(email)) {
            if (registeredOrganizers.get(email).equals(password)) {
                loggedIn = true;
                System.out.println("Welcome! You are logged in as an Organizer");
                return true;
            } else {
                System.out.println("Incorrect password.");
            }
        } else {
            System.out.println("No organizer found with this email.");
        }
        return false;
    }

    public void logOff() {
        if (loggedIn) {
            System.out.println("You have successfully logged off.");
            loggedIn = false;
        } else {
            System.out.println("No organizer is currently logged in.");
        }
    }
}