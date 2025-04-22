package org.jmc.pintfinder;

/**
 * Bar class used for manipulating bar data
 */
public class Bar {
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private double rating; // out of 10.0
    private double numRatings;


    /**
     * default constructor
     */
    public Bar() {}

    /**
     * args constructor used to create bars with all their info
     * @param name
     * @param latitude
     * @param longitude
     * @param rating
     */
    public Bar(String name, double latitude, double longitude, double rating) {

        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.numRatings = numRatings;

    }

    // Getters and Setters


    /**
     * get name
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * set name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get LAT
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * set LAT
     * @param latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * get LONG
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * set LONG
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * get Rating
     * @return
     */
    public double getRating() {
        return rating;
    }

    /**
     * set Rating
     * @param rating
     */
    public void setRating(double rating) {
        if (rating >= 0.0 && rating <= 10.0) {
            this.rating = rating;
        } else {
            throw new IllegalArgumentException("Rating must be between 0.0 and 10.0");
        }
    }

    /**
     * get number of ratings
     * @return
     */
    public double getNumRatings() {
        return numRatings;
    }

    /**
     * set Number of ratings
     * @param numRatings
     */
    public void setNumRatings(double numRatings) {
        this.numRatings = numRatings;
    }

    /**
     * show bar name and its rating
     * @return
     */
    @Override
    public String toString() {
        return name + " (Rating: " + rating + "/10)";
    }
}

