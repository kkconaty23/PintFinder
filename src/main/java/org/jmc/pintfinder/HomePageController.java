package org.jmc.pintfinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javafx.scene.transform.Rotate.X_AXIS;

public class HomePageController {
    @FXML
    public Button createAcctBtn;

    @FXML
    private Label accountbtn;

    @FXML
    private Label welcomeText;

    @FXML
    private TextField emailID;

    @FXML
    private Button forgotBtn;

    @FXML
    private PasswordField passwordID;

    @FXML
    private Button signInBtn;

    @FXML
    private CheckBox checkBox;

    @FXML
    private Label warningLabel;

    @FXML
    private WebView mapView;

    //    for animation
    @FXML
    private RotateTransition rotate;

    //    for animation
    @FXML
    private Timeline timeline;

    //    for animation
    @FXML
    private DropShadow shadow;

    @FXML
    private Pane profileBtn;

    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;

    @FXML
    private VBox reviewList;
    @FXML
    private TextArea reviewInput;
    @FXML
    private Slider ratingCombo;
    @FXML
    private Label comboLabel;

    private final Map<String, List<String>> locationReviews = new HashMap<>();
    private final Map<String, List<Double>> locationRatings = new HashMap<>();
    private String currentLocation = null;

    @FXML
    private Rectangle ratingIndicator;
    @FXML
    private Label averageOverlay;
    @FXML
    private Button submitReview;

    private List<Bar> barList = new ArrayList<>();

    /**
     * initialize method to load up all the important features before the fxml
     */
    @FXML
    public void initialize() {
//        loadBars();//METHOD USED TO LOAD FIREBASE WITH THE BARS(ONLY USE FORE RESETTING)
        FirebaseBarUploader.uploadBars(barList);//refresh the bars in fire base

        URL mapUrl = getClass().getResource("/map.html");
        if (mapUrl != null) {
            System.out.println("Loading: " + mapUrl.toExternalForm());

            WebEngine webEngine = mapView.getEngine();
            webEngine.load(mapUrl.toExternalForm());

            // Create the Java bridge
            MapBridge bridge = new MapBridge();
            bridge.setOnTitleChange(title -> Platform.runLater(() -> titleLabel.setText(title)));
            bridge.setOnDescriptionChange(desc -> Platform.runLater(() -> descriptionLabel.setText(desc)));
            //the bar with focus also gets the bar info from the DB
            bridge.setOnLocationSwitch(locationName -> {
                currentLocation = locationName;
                Platform.runLater(() -> {
                    // Fetch bar data including ratings from Firebase
                    fetchBarDataFromFirebase(locationName);
                });
            });

            // Hook the bridge into the JS context after the page loads
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", bridge);
                    System.out.println("Java bridge connected to JS.");
                }
            });
            ratingCombo.setMin(0);
            ratingCombo.setMax(10);
            ratingCombo.setValue(10);//TODO: set default rating to bar average
            ratingCombo.setShowTickLabels(true);
            // creating a listener to update the label when the slider value changes
            ratingCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
                comboLabel.setText("Score: " + String.format("%.1f", newValue.doubleValue()));
            });
            ratingCombo.setMajorTickUnit(2);
            ratingCombo.setMinorTickCount(0);
            submitReview.setOnAction(event -> handleSubmitReview());

            // Bind the Enter key to the submitReview action
            reviewInput.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode().toString().equals("ENTER")) {
                    handleSubmitReview();
                    keyEvent.consume(); // prevents adding a new line in the TextArea
                }
            });

        } else {
            System.out.println("map.html not found in resources!");
        }
    }

    /**
     * method to get the bar data from firebase based on what bar is clicked on
     * @param barName
     */
    private void fetchBarDataFromFirebase(String barName) {
        DatabaseReference barRef = FirebaseDatabase.getInstance()
                .getReference("bars")
                .child(barName);

        barRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Clear existing local data for this location
                    locationReviews.computeIfAbsent(barName, k -> new ArrayList<>()).clear();
                    locationRatings.computeIfAbsent(barName, k -> new ArrayList<>()).clear();

                    // Get overall rating
                    Double rating = snapshot.child("rating").getValue(Double.class);
                    Long numRatings = snapshot.child("numRatings").getValue(Long.class);

                    if (rating != null && numRatings != null) {
                        // Display the overall rating
                        Platform.runLater(() -> {
                            updateRatingDisplay(rating, numRatings);
                        });
                    }

                    // Clear existing reviews display
                    Platform.runLater(() -> {
                        reviewList.getChildren().clear();
                    });

                    // text reviews
                    DataSnapshot reviewsSnapshot = snapshot.child("reviews");
                    if (reviewsSnapshot.exists()) {
                        //create a list to store all reviews for sorting
                        List<ReviewItem> allReviews = new ArrayList<>();

                        for (DataSnapshot reviewSnapshot : reviewsSnapshot.getChildren()) {
                            String text = reviewSnapshot.child("text").getValue(String.class);
                            Double reviewRating = reviewSnapshot.child("rating").getValue(Double.class);
                            Long timestamp = reviewSnapshot.child("timestamp").getValue(Long.class);

                            if (text != null && reviewRating != null) {
                                // Add to local data collection
                                locationReviews.get(barName).add(String.format("%2.1f | %s", reviewRating, text));
                                locationRatings.get(barName).add(reviewRating);

                                // Add to sorting list
                                allReviews.add(new ReviewItem(text, reviewRating, timestamp != null ? timestamp : 0L));
                            }
                        }

                        // Sort reviews by newest first
                        allReviews.sort((r1, r2) -> Long.compare(r2.timestamp, r1.timestamp));

                        // Display reviews in UI
                        Platform.runLater(() -> {
                            displayReviews(allReviews);
                        });
                    } else {
                        // No reviews found
                        Platform.runLater(() -> {
                            displayNoReviews();
                        });
                    }
                } else {
                    // No data exists for this bar
                    Platform.runLater(() -> {
                        clearRatingDisplay();
                        displayNoReviews();
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error fetching bar data: " + error.getMessage());
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Error loading reviews. Please try again.");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                    reviewList.getChildren().clear();
                    reviewList.getChildren().add(errorLabel);
                });
            }
        });
    }

    /**
     * review class stores the reviews as objects
     */
    private static class ReviewItem {
        String text;
        double rating;
        long timestamp;

        /**
         * review constructor
         * @param text
         * @param rating
         * @param timestamp
         */
        ReviewItem(String text, double rating, long timestamp) {
            this.text = text;
            this.rating = rating;
            this.timestamp = timestamp;
        }
    }

    /**
     * Display reviews in the side panel
     * @param reviews List of review items to display
     */
    private void displayReviews(List<ReviewItem> reviews) {
        reviewList.getChildren().clear();

        // Add header
        Label headerLabel = new Label(String.format("%d Review%s", reviews.size(), reviews.size() == 1 ? "" : "s"));
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        headerLabel.setPadding(new Insets(0, 0, 10, 0));
        reviewList.getChildren().add(headerLabel);

        if (reviews.isEmpty()) {
            displayNoReviews();
            return;
        }

        // Add each review
        for (ReviewItem review : reviews) {
            VBox reviewCard = createReviewCard(review);
            reviewList.getChildren().add(reviewCard);

            // Add separator between reviews
            if (reviews.indexOf(review) < reviews.size() - 1) {
                Separator separator = new Separator();
                separator.setPadding(new Insets(8, 0, 8, 0));
                reviewList.getChildren().add(separator);
            }
        }
    }

    /**
     * Create a styled review card when someone leaves a review
     * @param review The review item to display
     * @return A styled VBox containing the review
     */
    private VBox createReviewCard(ReviewItem review) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(8));

        // Rating display
        HBox ratingDisplay = new HBox(5);

        // Create rating label
        Label ratingLabel = new Label(String.format("%.1f", review.rating));
        ratingLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e67e22;");





        ratingDisplay.getChildren().addAll(ratingLabel);

        // Review text
        Label textLabel = new Label(review.text);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8B4513");

        // Date
        if (review.timestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
            String dateStr = sdf.format(new Date(review.timestamp));
            Label dateLabel = new Label(dateStr);
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: BLACK;");
            card.getChildren().addAll(ratingDisplay, textLabel, dateLabel);
        } else {
            card.getChildren().addAll(ratingDisplay, textLabel);
        }

        return card;
    }

    /**
     * Display a message when no reviews are available
     */
    private void displayNoReviews() {
        reviewList.getChildren().clear();

        VBox noReviewsBox = new VBox(10);
        noReviewsBox.setAlignment(Pos.CENTER);
        noReviewsBox.setPadding(new Insets(20));

        Label noReviewsLabel = new Label("No Reviews Yet");
        noReviewsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label promptLabel = new Label("Be the first to share your experience!");
        promptLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        noReviewsBox.getChildren().addAll(noReviewsLabel, promptLabel);
        reviewList.getChildren().add(noReviewsBox);
    }


    /**
     * method that updates the pint glass based on ratings
     * @param rating
     * @param numRatings
     */
    private void updateRatingDisplay(double rating, long numRatings) {
        // update rating indicator
        double averageRatio = rating / 10.0;

        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, null,
                new Stop(0, Color.web(calculateColor(averageRatio))),
                new Stop(1, Color.RED)
        );

        ratingIndicator.setFill(gradient);
        ratingIndicator.setHeight(averageRatio * 100 + 16);
        ratingIndicator.setTranslateY((1.0 - averageRatio) * 100);

        //update text display
        averageOverlay.setText(String.format("%.1f", rating));


        ratingCombo.setValue(rating);
    }

    /**
     * method to clear the rating display when no data exists
     */
    private void clearRatingDisplay() {

        ratingIndicator.setFill(Color.TRANSPARENT);
        averageOverlay.setText("No Ratings");
        reviewList.getChildren().clear();

        // Reset slider to default
        ratingCombo.setValue(5.0);
    }

    @FXML
    public void signAnimationStart(MouseEvent event) {
        Pane pane = (Pane) event.getSource();

        rotate = new RotateTransition(Duration.seconds(.85), pane);
        rotate.setFromAngle(pane.getRotate());
        rotate.setToAngle(-25);
        rotate.setCycleCount(1);
        rotate.setAxis(X_AXIS);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        shadow = new DropShadow();
        shadow.setWidth(21);
        shadow.setHeight(35.66);
        shadow.setRadius(13.67);
        shadow.setOffsetX(5);
        shadow.setSpread(0.28);
        pane.setEffect(shadow);

        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(shadow.radiusProperty(), 10)),
                new KeyFrame(Duration.ZERO, new KeyValue(shadow.heightProperty(), 36)),
                new KeyFrame(Duration.seconds(0.85), new KeyValue(shadow.radiusProperty(), 45)),
                new KeyFrame(Duration.seconds(0.85), new KeyValue(shadow.heightProperty(), 69)),
                new KeyFrame(Duration.seconds(0.85), new KeyValue(shadow.radiusProperty(), 5)),
                new KeyFrame(Duration.seconds(0.85), new KeyValue(shadow.heightProperty(), 15))

        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);

        rotate = new RotateTransition(Duration.seconds(.85), pane);
        rotate.setFromAngle(-25);
        rotate.setToAngle(35);
        rotate.setAxis(X_AXIS);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setAutoReverse(true);
        rotate.setInterpolator(Interpolator.EASE_BOTH);

        timeline.play();
        rotate.play();
    }

    @FXML
    public void signAnimationEnd(MouseEvent event) {
        Pane pane = (Pane) event.getSource();

        rotate.stop();
        timeline.stop();
        rotate = new RotateTransition(Duration.seconds(.85), pane);
        rotate.setAxis(X_AXIS);
        rotate.setToAngle(0);
        rotate.setCycleCount(1);

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(.85), new KeyValue(shadow.widthProperty(), 21)),
                new KeyFrame(Duration.seconds(.85), new KeyValue(shadow.heightProperty(), 35.66)),
                new KeyFrame(Duration.seconds(.85), new KeyValue(shadow.radiusProperty(), 13.67)),
                new KeyFrame(Duration.seconds(.85), new KeyValue(shadow.offsetXProperty(), 5)),
                new KeyFrame(Duration.seconds(.85), new KeyValue(shadow.spreadProperty(), .28))
        );
        timeline.setCycleCount(1);

        timeline.play();
        rotate.play();

    }

    @FXML
    void bringToAccount(MouseEvent event) throws IOException {

        FXMLLoader fxmlProfileLoader = new FXMLLoader(Login.class.getResource("profile.fxml"));

        Scene ProfileScene = new Scene(fxmlProfileLoader.load(), 600, 400);

        Stage ProfileStage = new Stage();
        ProfileStage.setTitle("Account Page");
        ProfileStage.setScene(ProfileScene);
        ProfileStage.getIcons().add(new Image("file:src/main/resources/img/PintFinder_Logo.png"));//sets favicon
        ProfileStage.setResizable(false);
        ProfileStage.show();


        Stage currentStage = (Stage) profileBtn.getScene().getWindow();
        currentStage.close();

    }


    /**
     * updated review button click to submit the review to fire base
     */
    @FXML
    private void handleSubmitReview() {
        String text = reviewInput.getText().trim();
        double newRating = Math.round(ratingCombo.getValue() * 10.0) / 10.0;

        if (!text.isEmpty() && currentLocation != null) {
            String fullReview = String.format("%2.1f | %s", newRating, text);

            // Local map updates
            locationReviews.computeIfAbsent(currentLocation, k -> new ArrayList<>()).add(fullReview);
            locationRatings.computeIfAbsent(currentLocation, k -> new ArrayList<>()).add(newRating);

            reviewList.getChildren().add(new Label(fullReview));
            updateAverageRating(currentLocation);

            // Firebase DB reference to the bar
            DatabaseReference barRef = FirebaseDatabase.getInstance()
                    .getReference("bars")//uses the bars table
                    .child(currentLocation);

            // Generate a unique ID for this review
            DatabaseReference reviewsRef = barRef.child("reviews").push();
            String reviewId = reviewsRef.getKey();

            //create review object as a hashmap
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("text", text);
            reviewData.put("rating", newRating);
            reviewData.put("timestamp", ServerValue.TIMESTAMP);

            // Read current rating and numRatings, then update
            barRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    // Create a transaction to ensure atomic updates
                    Map<String, Object> updates = new HashMap<>();

                    if (snapshot.exists()) {
                        Double oldRating = snapshot.child("rating").getValue(Double.class);//QUERYING THE DB
                        Long numRatings = snapshot.child("numRatings").getValue(Long.class);//QUERYING THE DB

                        if (oldRating == null) oldRating = 0.0;
                        if (numRatings == null) numRatings = 0L;

                        long newNumRatings = numRatings + 1;
                        double avg = Math.round(((oldRating * numRatings + newRating) / newNumRatings) * 10.0) / 10.0;

                        //update aggregate rating data in DB
                        updates.put("rating", avg);
                        updates.put("numRatings", newNumRatings);
                    } else {
                        // Bar doesn't exist yet, initialize it
                        updates.put("rating", newRating);
                        updates.put("numRatings", 1L);
                        updates.put("name", currentLocation); // Assuming currentLocation is the bar name
                    }

                    //add the new review to the DB
                    updates.put("reviews/" + reviewId, reviewData);

                    //apply all updates
                    barRef.updateChildren(updates, (error, ref) -> {
                        if (error != null) {
                            System.err.println("Failed to update bar data: " + error.getMessage());
                        } else {
                            System.out.println("Bar rating and review successfully updated");
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Failed to read or update rating: " + error.getMessage());
                }
            });

            reviewInput.clear();
            ratingCombo.setValue(10);
        }
    }



    private void updateAverageRating(String locationName) {
        List<Double> ratings = locationRatings.getOrDefault(locationName, new ArrayList<>());

        if (ratings.isEmpty()) {
            ratingIndicator.setFill(Color.TRANSPARENT);
            averageOverlay.setText("N/A");
            ratingCombo.setValue(0);
            return;
        }

        double avg = ratings.stream().mapToDouble(i -> i).average().orElse(0.0);

        double averageRatio = avg / 10.0; // Assuming 'avg' is a value between 0 and 10


        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, null,
                new Stop(0, Color.web(calculateColor(averageRatio))),
                new Stop(1, Color.RED)
        );
        ratingIndicator.setFill(gradient);
        ratingIndicator.setHeight(averageRatio * 100+16); // Adjust width based on average ratio
        ratingIndicator.setTranslateY((1.0-averageRatio) * 100);
        averageOverlay.setText(String.format("%.1f", avg));

    }

    private String calculateColor(double ratio) {
        // Ensure ratio is within 0-1 range
        ratio = Math.max(0, Math.min(ratio, 1));

        // Calculate RGB components.  This example transitions from green to red.
        int red = (int) (255 * (1-ratio));
        int green = (int) (255 * (ratio));
        int blue = 0;

        // Format as a hex string.
        return String.format("#%02X%02X%02X", red, green, blue);
    }
    /**
     * method use to create bar object from all the bars on the map
     * ONCE NEW BARS ARE ADDED WE DONT NEED TO USE THIS METHOD (WILL REMOVE RATINGS)
     */
    private void loadBars() {
        barList.add(new Bar("Oakdale Brew House", 40.7386, -73.1192, 0));
        barList.add(new Bar("Artemis", 40.8097, -73.1063, 0));
        barList.add(new Bar("Toomey's Tavern", 40.6624, -73.4240, 0));
        barList.add(new Bar("Johnny McGorey's Pub", 40.6782, -73.4548, 0));
        barList.add(new Bar("Portly Villager", 40.7328, -73.0886, 0));
        barList.add(new Bar("Destination Unknown Beer Company", 40.73393, -73.2322, 0));
        barList.add(new Bar("Momos Too Sports Bar & Grill", 40.7472, -73.0578, 0));
        barList.add(new Bar("Sand City Brewing Company", 40.900136, -73.3535, 0));
    }

    /**
     * loads the existing bars to display ratings
     */
    public static class FirebaseBarUploader {
        public static void uploadBars(List<Bar> barList) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("bars");

            for (Bar bar : barList) {
                ref.child(String.valueOf(bar.getName())).setValueAsync(bar);
            }
        }

    }
}





