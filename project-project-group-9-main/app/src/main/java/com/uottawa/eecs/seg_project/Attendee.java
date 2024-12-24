package com.uottawa.eecs.seg_project;

import java.util.HashMap;
import java.util.Map;
public class Attendee extends User{
    static Map<String, String> registeredAttendees = new HashMap<>();
    private static boolean loggedIn = false;

    public Attendee (String firstName, String lastName, String email, String password, String phoneNumber, String address){
        super(firstName,lastName,email,password,phoneNumber,address);
    }

    public boolean registerUser(){
        if (registeredAttendees.containsKey(email)){
            System.out.println("Attendee already registered with this email.");
            return false;
        }
        registeredAttendees.put(email,password);
        System.out.println("Attendee successfully registered.");
        return true;
    }

    public boolean logIn(String email, String password){
        if (registeredAttendees.containsKey(email)){
            if (registeredAttendees.get(email).equals(password)){
                loggedIn = true;
                System.out.println("Welcome!! You're logged in as an attendee");
                return true;
            } else{
                System.out.println("Incorrect password.");
            }

        } else{
            System.out.println("No attendee found with this email.");
        }
        return false;
    }


    public void logOff(){
        if (loggedIn){
            System.out.println("You have successfully logged off.");
            loggedIn = false;
        }else{
            System.out.println("No attendee is currently logged in.");
        }
    }
}