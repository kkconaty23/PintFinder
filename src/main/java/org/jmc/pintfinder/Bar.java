package org.jmc.pintfinder;

import java.util.ArrayList;

public class Bar {
    private static int idCounter = 0;
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private double avgRating;
    private ArrayList<String> pastReviews;
    private ArrayList<Integer> pastScores;
    private int numReviews;

    //todo: potentially make it so that the score is linked specifically to the comment/reveiw

    public Bar(String name, double latitude, double longitude, double avgRating) {
        this.id = ++idCounter; // auto-incrementing ID
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.avgRating = avgRating;
        this.pastReviews = new ArrayList<>();
        this.pastScores = new ArrayList<>();
        this.numReviews = 0;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double[] getCords() {
        return new double[]{latitude, longitude};
    }
    public double getAvgRating() {
        return avgRating;
    }
    public int getNumReviews() {
        return numReviews;
    }
    public ArrayList<String> getPastReviews() {
        return pastReviews;
    }
    public ArrayList<Integer> getPastScores() {
        return pastScores;
    }

    public void addReview(String review, int score) {
        this.pastReviews.add(review);
        this.pastScores.add(score);
        this.numReviews++;
        updateAvgRating();
    }
    public void addReview(int score) {
        this.pastScores.add(score);
        this.numReviews++;
        updateAvgRating();
    }
    public void addReview(String review) {
        this.pastReviews.add(review);
        this.numReviews++;
    }

    private void updateAvgRating() {
        double total = 0;
        for (int score : pastScores) {
            total += score;
        }
        this.avgRating = total / numReviews;
    }

}