package com.uottawa.eecs.seg_project;
import java.util.List;
import java.util.ArrayList;

public class Administrator extends User {

    //These lists hold the different types of requests
    List<RegistrationRequest> pendingRequests = new ArrayList<>();
    List<RegistrationRequest> approvedRequests = new ArrayList<>();
    List<RegistrationRequest> deniedRequests = new ArrayList<>();

    private static final String ADMIN_PASSWORD = "Admin123";

    public Administrator() {
    }

    public boolean logIn (String email, String password){
        if (password.equals(ADMIN_PASSWORD)) {
            System.out.println("Welcome!! You're logged in as an administrator.");
            return true;
        } else {
            System.out.println("Invalid login credentials. Please try again.");
            return false;
        }
    }

    public void logOff(){
        System.out.println("You've logged off");
    }


    // This method checks if the pending requests list is empty and if its not we just print the pending requesuts
    public void viewPendingRequests(){
        if (pendingRequests.isEmpty()){
            System.out.println("There are no pending requests at the moment.");

        }else{
            for(int i = 0; i<pendingRequests.size();i++){
                System.out.println(pendingRequests.get(i));

            }

        }
    }

    //This method approves requests (Note: You have to specify the index of the person you want to approve from the pending requests list)
    public void approvedRequests(int indexOfRequest){
        if (indexOfRequest >=0 && indexOfRequest < pendingRequests.size()){
            RegistrationRequest request = pendingRequests.remove(indexOfRequest);
            approvedRequests.add(request);
            System.out.println(request.getFirstName() + request.getFirstName() + " has been approved");

        }else{
            System.out.println("Not today buddy...");
        }
    }

    public void deniedRequests(int indexOfRequest){
        if(indexOfRequest >=0 && indexOfRequest < pendingRequests.size()){
            RegistrationRequest request = pendingRequests.remove(indexOfRequest);
            deniedRequests.add(request);
            System.out.println(request.getFirstName() + request.getFirstName() + " has been rejected, please contact 613-613-6134 to contact support.");

        }else{
            System.out.println("Not today buddy...");
        }
    }

    public void viewRejectedRequests(){
        if (deniedRequests.isEmpty()){
            System.out.println("No rejected requests");
        }else{
            for (int i =0; i < deniedRequests.size(); i++){
                System.out.println(deniedRequests.get(i));
            }
        }
    }

    public void changeDecision(int index){
        if(index>=0 && index > deniedRequests.size()){
            RegistrationRequest request = deniedRequests.remove(index);
            approvedRequests.add(request);
            System.out.println(request.getFirstName() + request.getFirstName() + "has been approved");
        }else{
            System.out.println("Not today buddy!");
        }
    }
}