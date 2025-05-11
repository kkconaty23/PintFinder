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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javafx.scene.transform.Rotate.X_AXIS;
/**
 * Controller for the main homepage of the PintFinder application.
 * Manages bar interactions, review system, animations, and Firebase integration.
 */
public class HomePageController {

    @FXML private WebView mapView;

    //    for animation
    @FXML private RotateTransition rotate;

    //    for animation
    @FXML private Timeline timeline;

    //    for animation
    @FXML private DropShadow shadow;


    // Labels
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label comboLabel;
    @FXML private Label averageOverlay;
    @FXML private Label barOfTheDayName;

    @FXML private VBox reviewList;
    @FXML private TextArea reviewInput;
    @FXML private Slider ratingCombo;

    private final Map<String, List<String>> locationReviews = new HashMap<>();
    private final Map<String, List<Double>> locationRatings = new HashMap<>();
    private String currentLocation = null;

    @FXML
    private Rectangle ratingIndicator;

    @FXML
    private Button submitReview;

    private List<Bar> barList = new ArrayList<>();

    // Panes
    @FXML private Pane barOfTheDay;
    @FXML private Pane profileBtn;
    private Bar barOfTheDayData;
    private WebEngine webEngine;

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

            webEngine = mapView.getEngine();
            webEngine.load(mapUrl.toExternalForm());
            loadBarOfTheDay();

            // Create the Java bridge
            MapBridge bridge = new MapBridge();
            bridge.setOnTitleChange(title -> Platform.runLater(() -> titleLabel.setText(title)));
            bridge.setOnDescriptionChange(desc -> Platform.runLater(() -> descriptionLabel.setText(desc)));
            bridge.setOnLocationSwitch(locationName -> {
                currentLocation = locationName;
                Platform.runLater(() -> {
                    fetchBarDataFromFirebase(locationName);
                });
            });

            // Store reference to WebEngine in the bridge
            bridge.setWebEngine(webEngine);

            // Hook the bridge into the JS context and inject geolocation polyfill
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", bridge);
                    System.out.println("Java bridge connected to JS.");

                    // Inject geolocation polyfill
                    try {
                        webEngine.executeScript(
                                "navigator.geolocation = navigator.geolocation || {};" +
                                        "navigator.geolocation.getCurrentPosition = function(success, error, options) {" +
                                        "    console.log('Geolocation requested');" +
                                        "    javaBridge.getUserLocation();" +
                                        "    window.tempGeolocationCallback = success;" +
                                        "};"
                        );
                        System.out.println("Geolocation polyfill injected successfully");
                    } catch (Exception e) {
                        System.err.println("Failed to inject geolocation polyfill: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            loadBarOfTheDay();

            // Create the Java bridge

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
            ratingCombo.setValue(10);
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

                        //for each review available get the rating and text review
                        for (DataSnapshot reviewSnapshot : reviewsSnapshot.getChildren()) {
                            String text = reviewSnapshot.child("text").getValue(String.class);
                            Double reviewRating = reviewSnapshot.child("rating").getValue(Double.class);
                            Long timestamp = reviewSnapshot.child("timestamp").getValue(Long.class);
                            String reviewer = reviewSnapshot.child("reviewer").getValue(String.class);
                            if (reviewer == null) reviewer = "Anonymous";

                            if (text != null && reviewRating != null) {
                                allReviews.add(new ReviewItem(text, reviewRating, timestamp != null ? timestamp : 0L, reviewer));
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

    private static class ReviewItem {
        String text;
        double rating;
        long timestamp;
        String reviewer;

        ReviewItem(String text, double rating, long timestamp, String reviewer) {
            this.text = text;
            this.rating = rating;
            this.timestamp = timestamp;
            this.reviewer = reviewer;
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
        for (int i = 0; i < reviews.size(); i++) {
            TextFlow reviewCard = createReviewCard(reviews.get(i), i + 1);
            reviewList.getChildren().add(reviewCard);
        }
    }

    /**
     * Create a styled review card when someone leaves a review
     * @param review The review item to display
     * @return A styled VBox containing the review
     */
    private TextFlow createReviewCard(ReviewItem review, int index) {

        // Create text object with full review
        String fullReviewText = String.format("%.1f", review.rating) + " | " + review.text;
        Text textNode = new Text(fullReviewText);
        textNode.setStyle("-fx-font-size: 14px; -fx-fill: #cccccc;");

        TextFlow textFlow = new TextFlow(textNode);
        textFlow.setPrefWidth(240); // Slightly under ScrollPane to allow padding
        textFlow.setLineSpacing(2);
        textFlow.getStyleClass().add("reviewLooks");

        // Tooltip on hover
        String dateStr = review.timestamp > 0
                ? new SimpleDateFormat("MMM d, yyyy").format(new Date(review.timestamp))
                : "Unknown Date";
        Tooltip tooltip = new Tooltip("Reviewed by: " + review.reviewer + "\nDate: " + dateStr);
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(textFlow, tooltip);
        return textFlow;
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
                new Stop(0, Color.web("#F1E5C0")), // creamy top
                new Stop(0.3, Color.web("#1C0A00")) // dark stout body (dominant)

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
    /**
     * Triggers the swinging animation and drop shadow on hover of the bar sign.
     * @param event Mouse event from entering the sign area
     */
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
    /**
     * Resets the sign animation and shadow effects when mouse leaves the element.
     * @param event Mouse event from leaving the sign area
     */
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
    /**
     * Navigates to the user’s profile page from the homepage.
     * @param event Mouse event from clicking profile button
     * @throws IOException if the profile FXML fails to load
     */
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
     * Handles the review submission: updates bar ratings and stores review in Firebase.
     */
    @FXML
    private void handleSubmitReview() {
        String text = reviewInput.getText().trim();
        double newRating = Math.round(ratingCombo.getValue() * 10.0) / 10.0;

        if (!text.isEmpty() && currentLocation != null) {
            String uid = SessionManager.getCurrentUserUid();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot userSnapshot) {
                    String reviewerName = userSnapshot.child("firstName").getValue(String.class);
                    if (reviewerName == null) reviewerName = "Anonymous";

                    DatabaseReference barRef = FirebaseDatabase.getInstance().getReference("bars").child(currentLocation);
                    DatabaseReference reviewsRef = barRef.child("reviews").push();
                    String reviewId = reviewsRef.getKey();

                    Map<String, Object> reviewData = new HashMap<>();
                    reviewData.put("text", text);
                    reviewData.put("rating", newRating);
                    reviewData.put("timestamp", ServerValue.TIMESTAMP);
                    reviewData.put("reviewer", reviewerName);

                    barRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Map<String, Object> updates = new HashMap<>();

                            if (snapshot.exists()) {
                                Double oldRating = snapshot.child("rating").getValue(Double.class);
                                Long numRatings = snapshot.child("numRatings").getValue(Long.class);

                                if (oldRating == null) oldRating = 0.0;
                                if (numRatings == null) numRatings = 0L;

                                long newNumRatings = numRatings + 1;
                                double avg = Math.round(((oldRating * numRatings + newRating) / newNumRatings) * 10.0) / 10.0;

                                updates.put("rating", avg);
                                updates.put("numRatings", newNumRatings);
                            } else {
                                updates.put("rating", newRating);
                                updates.put("numRatings", 1L);
                                updates.put("name", currentLocation);
                            }

                            updates.put("reviews/" + reviewId, reviewData);

                            barRef.updateChildren(updates, (error, ref) -> {
                                if (error != null) {
                                    System.err.println("Failed to update bar data: " + error.getMessage());
                                } else {
                                    System.out.println("Bar rating and review successfully updated");

                                    Platform.runLater(() -> {
                                        reviewInput.clear();
                                        ratingCombo.setValue(10);

                                        // 🔄 Refresh the full list of reviews from DB
                                        fetchBarDataFromFirebase(currentLocation);
                                    });
                                }
                            });

                            DatabaseReference userReviewRef = userRef.child("reviews").child(reviewId);

                            Map<String, Object> userReviewData = new HashMap<>();
                            userReviewData.put("barName", currentLocation);
                            userReviewData.put("text", text);
                            userReviewData.put("rating", newRating);
                            userReviewData.put("timestamp", ServerValue.TIMESTAMP);

                            userReviewRef.setValueAsync(userReviewData);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            System.err.println("Failed to read or update rating: " + error.getMessage());
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Failed to fetch user info: " + error.getMessage());
                }
            });
        }
    }
    /**
     * Updates the average rating color and text based on all ratings for a location.
     * @param locationName the name of the bar location
     */
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
    /**
     * Calculates the hex color value based on a 0–1 rating ratio.
     * @param ratio a decimal between 0 and 1
     * @return a hex color string
     */
    private String calculateColor(double ratio) {
        // Clamp ratio to 0-1
        ratio = Math.max(0, Math.min(ratio, 1));

        // Guinness colors: dark ruby (low), creamy tan (high)
        int startR = 28, startG = 10, startB = 0;      // #1C0A00
        int endR = 241, endG = 229, endB = 192;        // #F1E5C0

        int red = (int) (startR + ratio * (endR - startR));
        int green = (int) (startG + ratio * (endG - startG));
        int blue = (int) (startB + ratio * (endB - startB));

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

    /**
     * Creates a visual card for a top-rated bar.
     * @param bar the bar and its rating
     * @param index rank index in the top list
     * @return a styled TextFlow containing the bar info
     */
    private TextFlow createBarCard(BarWithRating bar, int index) {
        String fullBarText = String.format("%.1f", bar.rating) + " | " + bar.name;
        Text textNode = new Text(fullBarText);
        textNode.setStyle("-fx-font-size: 14px; -fx-fill: #cccccc;");

        TextFlow textFlow = new TextFlow(textNode);
        textFlow.setPrefWidth(240); // Consistent with reviews
        textFlow.setLineSpacing(2);
        textFlow.getStyleClass().add("reviewLooks");

        // Optional tooltip (e.g., placeholder info)
        Tooltip tooltip = new Tooltip("Bar ranking #" + index);
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(textFlow, tooltip);

        return textFlow;
    }
    /**
     * Fetches bar ratings and displays the top 10 rated bars.
     * @param event mouse event triggering the fetch
     */
    @FXML
    private void fetchAndDisplayTopBars(MouseEvent event) {

        DatabaseReference barsRef = FirebaseDatabase.getInstance().getReference("bars");

        barsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<BarWithRating> bars = new ArrayList<>();

                    for (DataSnapshot barSnapshot : snapshot.getChildren()) {
                        String name = barSnapshot.child("name").getValue(String.class);
                        Double rating = barSnapshot.child("rating").getValue(Double.class);

                        if (name != null && rating != null) {
                            bars.add(new BarWithRating(name, rating));
                        }
                    }

                    // Sort by rating descending
                    bars.sort((b1, b2) -> Double.compare(b2.rating, b1.rating));

                    // Take top 10
                    List<BarWithRating> topBars = bars.stream().limit(10).collect(Collectors.toList());

                    Platform.runLater(() -> displayTopBars(topBars));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Failed to fetch top bars: " + error.getMessage());
            }
        });
    }

    // Helper class to store bar name and rating
    private static class BarWithRating {
        String name;
        double rating;

        BarWithRating(String name, double rating) {
            this.name = name;
            this.rating = rating;
        }
    }

    /**
     * Displays the list of top 10 bars in the review list area.
     * @param bars list of top-rated bars
     */
    private void displayTopBars(List<BarWithRating> bars) {
        reviewList.getChildren().clear(); // Clear sidebar

        // Add consistent header
        Label headerLabel = new Label("Top 10 Bars");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        headerLabel.setPadding(new Insets(0, 0, 10, 0));
        reviewList.getChildren().add(headerLabel);

        // Add each styled bar card
        for (int i = 0; i < bars.size(); i++) {
            TextFlow barCard = createBarCard(bars.get(i), i + 1);
            reviewList.getChildren().add(barCard);
        }

    }
    /**
     * Loads the current Bar of the Day, or picks a new one if expired.
     */
    private void loadBarOfTheDay() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("barOfTheDay");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long lastUpdated = snapshot.child("lastUpdated").getValue(Long.class);
                    String barId = snapshot.child("selectedBarId").getValue(String.class);

                    long now = System.currentTimeMillis();
                    long oneDay = 24 * 60 * 60 * 1000;

                    if (now - lastUpdated > oneDay) {
                        pickNewBarOfTheDay();
                    } else {
                        fetchBarOfTheDayDetails(barId);
                    }
                } else {
                    pickNewBarOfTheDay();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Failed to load Bar of the Day: " + error.getMessage());
            }
        });
    }
    /**
     * Randomly selects a new bar to be the Bar of the Day and updates Firebase.
     */
    private void pickNewBarOfTheDay() {
        DatabaseReference barsRef = FirebaseDatabase.getInstance().getReference("bars");

        barsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<DataSnapshot> allBars = new ArrayList<>();
                snapshot.getChildren().forEach(allBars::add);

                if (!allBars.isEmpty()) {
                    Random rand = new Random();
                    DataSnapshot selected = allBars.get(rand.nextInt(allBars.size()));
                    String selectedId = selected.getKey();

                    Map<String, Object> update = new HashMap<>();
                    update.put("selectedBarId", selectedId);
                    update.put("lastUpdated", System.currentTimeMillis());

                    FirebaseDatabase.getInstance().getReference("barOfTheDay").updateChildrenAsync(update);
                    ;
                    fetchBarOfTheDayDetails(selectedId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
    /**
     * Wraps text at word boundaries for display in UI.
     * @param text the string to wrap
     * @param maxLineLength max characters per line
     * @return line-broken string
     */
    public static String wrapTextSmart(String text, int maxLineLength) {
        if (text == null || text.isEmpty()) return "";

        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split(" ");
        int currentLineLength = 0;

        for (String word : words) {
            if (currentLineLength + word.length() > maxLineLength) {
                wrapped.append("\n");
                currentLineLength = 0;
            } else if (currentLineLength != 0) {
                wrapped.append(" ");
                currentLineLength += 1;
            }

            wrapped.append(word);
            currentLineLength += word.length();
        }

        return wrapped.toString();
    }
    /**
     * Fetches the Bar of the Day's details from Firebase and updates UI.
     * @param barId the Firebase ID of the selected bar
     */
    private void fetchBarOfTheDayDetails(String barId) {
        DatabaseReference barRef = FirebaseDatabase.getInstance().getReference("bars").child(barId);

        barRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                barOfTheDayData = snapshot.getValue(Bar.class);
                if (barOfTheDayData != null) {
                    Platform.runLater(() -> {
                        if (barOfTheDayName != null) {
                            barOfTheDayName.setText(wrapTextSmart(barOfTheDayData.getName(),21));
//                            barOfTheDayName.setText(wrapTextSmart("Sand City Brewing Company",21));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
    /**
     * Handles click on the Bar of the Day and centers the map on it.
     * @param event mouse click event
     */
    @FXML
    void onBarOfTheDayClicked(MouseEvent event) {
        if (barOfTheDayData != null && webEngine != null) {
            System.out.println("Bar of the Day clicked: " + barOfTheDayData.getName());

            String barNameLower = barOfTheDayData.getName().toLowerCase().replace("'", "\\'");
            String js = String.format(
                    "map.setView([%f, %f], 15);" +
                            "markers.forEach(m => { if (m.barName === '%s') m.fire('click'); });",
                    barOfTheDayData.getLatitude(),
                    barOfTheDayData.getLongitude(),
                    barNameLower
            );

            webEngine.executeScript(js);
        } else {
            System.out.println("Bar data or WebEngine not ready.");
        }
    }


    @FXML
    void barsNearYou(MouseEvent event) {
        if (webEngine != null) {
            System.out.println("Calling locateUser() JavaScript function");
            try {
                webEngine.executeScript("locateUser()");
            } catch (Exception e) {
                System.err.println("Error calling locateUser(): " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("WebEngine is not initialized.");
        }
    }


    }








