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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private ProgressIndicator ratingIndicator;
    @FXML
    private Label averageOverlay;
    @FXML
    private Button submitReview;

    private List<Bar> barList = new ArrayList<>();

    @FXML
    public void initialize() {
//        loadBars();//method to create all bar objects
        FirebaseBarUploader.uploadBars(barList);

        URL mapUrl = getClass().getResource("/map.html");
        if (mapUrl != null) {
            System.out.println("Loading: " + mapUrl.toExternalForm());

            WebEngine webEngine = mapView.getEngine();
            webEngine.load(mapUrl.toExternalForm());

            // Create the Java bridge
            MapBridge bridge = new MapBridge();
            bridge.setOnTitleChange(title -> Platform.runLater(() -> titleLabel.setText(title)));
            bridge.setOnDescriptionChange(desc -> Platform.runLater(() -> descriptionLabel.setText(desc)));
            bridge.setOnLocationSwitch(locationName -> {
                currentLocation = locationName;
                Platform.runLater(() -> {
                    loadReviewsForLocation(locationName);
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
                    keyEvent.consume(); // Prevents adding a new line in the TextArea
                }
            });

        } else {
            System.out.println("map.html not found in resources!");
        }
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

        Scene ProfileScene = new Scene(fxmlProfileLoader.load(), 800, 600);

        Stage ProfileStage = new Stage();
        ProfileStage.setTitle("Account Page");
        ProfileStage.setScene(ProfileScene);
        ProfileStage.getIcons().add(new Image("file:src/main/resources/img/PintFinder_Logo.png"));//sets favicon
        ProfileStage.show();


        Stage currentStage = (Stage) profileBtn.getScene().getWindow();
        currentStage.close();

    }


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
                    .getReference("bars")
                    .child(currentLocation);

            // Generate a unique ID for this review
            DatabaseReference reviewsRef = barRef.child("reviews").push();
            String reviewId = reviewsRef.getKey();

            // Create review object
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
                        Double oldRating = snapshot.child("rating").getValue(Double.class);
                        Long numRatings = snapshot.child("numRatings").getValue(Long.class);

                        if (oldRating == null) oldRating = 0.0;
                        if (numRatings == null) numRatings = 0L;

                        long newNumRatings = numRatings + 1;
                        double avg = Math.round(((oldRating * numRatings + newRating) / newNumRatings) * 10.0) / 10.0;

                        // Update aggregate rating data
                        updates.put("rating", avg);
                        updates.put("numRatings", newNumRatings);
                    } else {
                        // Bar doesn't exist yet, initialize it
                        updates.put("rating", newRating);
                        updates.put("numRatings", 1L);
                        updates.put("name", currentLocation); // Assuming currentLocation is the bar name
                    }

                    // Add the new review
                    updates.put("reviews/" + reviewId, reviewData);

                    // Apply all updates atomically
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


    private void loadReviewsForLocation(String location) {
        if (reviewList != null) {
            reviewList.getChildren().clear();
        }
        reviewList.getChildren().clear();

        List<String> reviews = locationReviews.getOrDefault(location, new ArrayList<>());

        for (String review : reviews) {
            Label label = new Label(review);
            label.setWrapText(true);
            reviewList.getChildren().add(label);
        }
    }

    private void updateAverageRating(String locationName) {
        List<Double> ratings = locationRatings.getOrDefault(locationName, new ArrayList<>());

        if (ratings.isEmpty()) {
            ratingIndicator.setProgress(0);
            averageOverlay.setText("N/A");
            return;
        }

        double avg = ratings.stream().mapToDouble(i -> i).average().orElse(0.0);
        ratingIndicator.setProgress(avg / 10.0); // progress is between 0.0 and 1.0
        averageOverlay.setText(String.format("%.1f", avg));
    }

    /**
     * method use to create bar object from all the bars on the map
     * ONCE NEW BARS ARE ADDED WE DONT NEED TO USE THIS METHOD
     */
    private void loadBars() {
        barList.add(new Bar( "Changing Times Pub!", 40.7481, -73.4290, 0.0));
        barList.add(new Bar( "Barrage Brewing Company", 40.6720, -73.5027, 0.0));
        barList.add(new Bar( "Small Craft Brewing Company", 40.6719, -73.4216, 0.0));
        barList.add(new Bar( "Icicle Brewing Company", 47.6001, -120.6595, 0.0));
        barList.add(new Bar( "Great South Bay Brewery", 40.7608, -73.2658, 0.0));
        barList.add(new Bar( "Oyster Bay Brewing Company", 40.8731, -73.5339, 0.0));
        barList.add(new Bar( "Destination Unknown Beer Company", 40.73393, -73.2322, 0.0));
        barList.add(new Bar( "The Blind Bat Brewery", 40.889694, -73.3874, 0.0));
        barList.add(new Bar( "Sand City Brewing Company", 40.900136, -73.3535, 0.0));
    }

    public static class FirebaseBarUploader {
        public static void uploadBars(List<Bar> barList) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("bars");

            for (Bar bar : barList) {
                ref.child(String.valueOf(bar.getName())).setValueAsync(bar);
            }
        }

    }
}





