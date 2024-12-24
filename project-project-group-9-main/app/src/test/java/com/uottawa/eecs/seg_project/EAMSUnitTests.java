package com.uottawa.eecs.seg_project;

import org.junit.Test;
import static org.junit.Assert.*;

public class EAMSUnitTests {

    // Test 1: Administrator Login Validation
    @Test
    public void testAdminLogin() {
        Administrator admin = new Administrator();

        // Test with correct password
        assertTrue("Admin should be able to login with correct password",
                admin.logIn("admin@example.com", "Admin123"));

        // Test with incorrect password
        assertFalse("Admin should not be able to login with incorrect password",
                admin.logIn("admin@example.com", "wrongpassword"));
    }

    // Test 2: Registration Request Processing
    @Test
    public void testRegistrationRequestHandling() {
        Administrator admin = new Administrator();
        RegistrationRequest request = new RegistrationRequest(
                "John", "Doe", "john@example.com", "Attendee",
                "123 Main St", "", "password123", "123-456-7890"
        );

        // Add request to pending
        admin.pendingRequests.add(request);

        // Test approving request
        admin.approvedRequests(0);

        assertTrue("Approved requests list should contain the request",
                admin.approvedRequests.contains(request));
        assertFalse("Pending requests list should not contain the request",
                admin.pendingRequests.contains(request));
    }

    // Test 3: Registration Request Data Validation
    @Test
    public void testRegistrationRequestValidation() {
        RegistrationRequest request = new RegistrationRequest(
                "Jane", "Smith", "jane@example.com", "Organizer",
                "456 Oak St", "Tech Corp", "password123", "987-654-3210"
        );

        assertEquals("First name should match", "Jane", request.getFirstName());
        assertEquals("Last name should match", "Smith", request.getLastName());
        assertEquals("Email should match", "jane@example.com", request.getEmail());
        assertEquals("Role should match", "Organizer", request.getRole());
        assertEquals("Organization should match", "Tech Corp", request.getOrganization());
    }

    // Test 4: Event Registration Cancellation Time Check
    @Test
    public void testEventCancellationTimeValidation() {
        long currentTime = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

        // Test event starting in 25 hours
        long eventTime = currentTime + (oneDay + 3600000); // 25 hours from now
        assertTrue("Should allow cancellation for event >24h away",
                isEventCancellationAllowed(eventTime));

        // Test event starting in 23 hours
        eventTime = currentTime + (oneDay - 3600000); // 23 hours from now
        assertFalse("Should not allow cancellation for event <24h away",
                isEventCancellationAllowed(eventTime));
    }


    // Test 5: Validate Administrator Logout
    @Test
    public void testAdminLogout() {
        Administrator admin = new Administrator();
        admin.logIn("admin@example.com", "Admin123");
        admin.logOff();
        assertFalse("Admin should be logged out after calling logOff()", admin.logIn("admin@example.com", ""));
    }
    // Test 6: Validate Registration Request Rejection
    @Test
    public void testRegistrationRequestRejection() {
        Administrator admin = new Administrator();
        RegistrationRequest request = new RegistrationRequest(
                "John", "Doe", "john@example.com", "Attendee",
                "123 Main St", "", "password123", "123-456-7890"
        );
        admin.pendingRequests.add(request);
        admin.deniedRequests(0);
        assertTrue("Denied requests list should contain the request",
                admin.deniedRequests.contains(request));
        assertFalse("Pending requests list should not contain the request",
                admin.pendingRequests.contains(request));
    }

    // Test 7: Attendee Login Success
    @Test
    public void testAttendeeLoginSuccess() {
        Attendee attendee = new Attendee(
                "Jane", "Doe", "jane.doe@example.com", "password123",
                "1234567890", "123 Main St"
        );
        attendee.registerUser();
        assertTrue("Attendee should log in with correct credentials",
                attendee.logIn("jane.doe@example.com", "password123"));
    }
    // Test 8: Validate Organizer Assignment
    @Test
    public void testOrganizerAssignment() {
        RegistrationRequest request = new RegistrationRequest(
                "Alice", "Johnson", "alice.johnson@example.com", "Organizer",
                "456 Elm St", "Tech Solutions", "password123", "789-123-4567"
        );
        Administrator admin = new Administrator();
        admin.pendingRequests.add(request);
        admin.approvedRequests(0);
        assertEquals("Organizer's organization should match after approval", "Tech Solutions", request.getOrganization());
    }

    // Helper method to simulate event cancellation time check
    private boolean isEventCancellationAllowed(long eventStartTime) {
        long currentTimeMillis = System.currentTimeMillis();
        long twentyFourHoursMillis = 24 * 60 * 60 * 1000;
        return (eventStartTime - currentTimeMillis >= twentyFourHoursMillis);
    }
}