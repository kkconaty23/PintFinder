package org.jmc.pintfinder;

public class Bar {
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private double rating; // out of 10.0
    private double numRatings;


    public Bar() {}

    public Bar(String name, double latitude, double longitude, double rating) {

        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.numRatings = numRatings;

    }

    // Getters and Setters


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        if (rating >= 0.0 && rating <= 10.0) {
            this.rating = rating;
        } else {
            throw new IllegalArgumentException("Rating must be between 0.0 and 10.0");
        }
    }

    public double getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(double numRatings) {
        this.numRatings = numRatings;
    }

    @Override
    public String toString() {
        return name + " (Rating: " + rating + "/10)";
    }
}

