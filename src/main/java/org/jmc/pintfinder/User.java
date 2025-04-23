package org.jmc.pintfinder;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String fName;
    private String lName;
    private String email;
    private String joinDate;
    private List<String> pastReveiws;

    User(String fName, String lName, String email) {
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.joinDate = java.time.LocalDate.now().toString();
        this.pastReveiws = new ArrayList<>();
    }

    public String getfName() {
        return fName;
    }
    public String getlName() {
        return lName;
    }
    public String getEmail() {
        return email;
    }
    public void setfName(String fName) {
        this.fName = fName;
    }
    public void setlName(String lName) {
        this.lName = lName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getJoinDate() {
        return joinDate;
    }
    public void addReview(String review) {
        this.pastReveiws.add(review);
    }
    public List<String> getPastReveiws() {
        return pastReveiws;
    }
}