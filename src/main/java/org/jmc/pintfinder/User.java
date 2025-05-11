package org.jmc.pintfinder;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents a user in the PintFinder application.
 * Stores basic user information such as name, email, join date, and past reviews.
 */
public class User {
    private String fName;
    private String lName;
    private String email;
    private String joinDate;
    private List<String> pastReveiws;
    /**
     * Constructs a new User with the given first name, last name, and email.
     * The join date is set to the current date.
     *
     * @param fName the user's first name
     * @param lName the user's last name
     * @param email the user's email address
     */
    User(String fName, String lName, String email) {
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.joinDate = java.time.LocalDate.now().toString();
        this.pastReveiws = new ArrayList<>();
    }
    /**
     * @return the user's first name
     */
    public String getfName() {
        return fName;
    }
    /**
     * @return the user's last name
     */
    public String getlName() {
        return lName;
    }
    /**
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }
    /**
     * Sets the user's first name.
     * @param fName new first name
     */
    public void setfName(String fName) {
        this.fName = fName;
    }
    /**
     * Sets the user's last name.
     * @param lName new last name
     */
    public void setlName(String lName) {
        this.lName = lName;
    }
    /**
     * Sets the user's email address.
     * @param email new email
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * @return the date the user joined the application
     */
    public String getJoinDate() {
        return joinDate;
    }
    /**
     * Adds a review ID or summary to the user's list of past reviews.
     * @param review the review to add
     */
    public void addReview(String review) {
        this.pastReveiws.add(review);
    }
    /**
     * @return a list of the user's past reviews
     */
    public List<String> getPastReveiws() {
        return pastReveiws;
    }
}