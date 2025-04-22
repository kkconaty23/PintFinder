package org.jmc.pintfinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
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
    public  Button createAcctBtn;

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

    @FXML private Pane profileBtn;

    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;

    @FXML private VBox reviewList;
    @FXML private TextArea reviewInput;
    @FXML private Slider ratingCombo;
    @FXML private Label comboLabel;

    private final Map<String, List<String>> locationReviews = new HashMap<>();
    private final Map<String, List<Double>> locationRatings = new HashMap<>();
    private String currentLocation = null;

    @FXML private ProgressIndicator ratingIndicator;
    @FXML private Label averageOverlay;
    @FXML private Button submitReview;

    @FXML
    public void initialize() {
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
                Platform.runLater(() -> { loadReviewsForLocation(locationName);});
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


    /**
     * added a create account button allows a new window to open where the user can be added to the firebase
     *
     * @param event
     */
    @FXML
    private void createAcctBtnClick(ActionEvent event) {//when a user creates account it makes them account that links to FB

        String email = emailID.getText();
        String password = passwordID.getText();


        if(! checkBox.isSelected()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            warningLabel.setText("Must Answer");
            return;
        }



        // create a user in Firebase
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            System.out.println("User created successfully: " + userRecord.getUid());//testing users
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating user: " + e.getMessage());
        }
    }

    public void signInBtnClick(ActionEvent actionEvent) {
        String email = emailID.getText();
        String password = passwordID.getText();

        if(email.isEmpty() || password.isEmpty()){
            warningLabel.setText("Please Enter Email and Password");
            return;
        }

        if(!checkBox.isSelected()){
            warningLabel.setText("You must agree to continue.");
            return;
        }

        System.out.println("Logging in user: " + email + "...");

        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Homepage.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) signInBtn.getScene().getWindow();
            stage.setTitle("PintFinder Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            warningLabel.setText("Error loading homepage.fxml");
        }

    }

    /**
     * registers the users email and password and puts them into firebase
     * @param event
     * @throws IOException
     */
    @FXML
    void makeActBtnClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newAccount.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Create Account");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleSubmitReview() {
        String text = reviewInput.getText().trim();
        double rating = Math.round(ratingCombo.getValue() * 10.0) / 10.0;//makes sure its 1 decimal

        if (!text.isEmpty()  && currentLocation != null) {
            // Add review text to review map
            String fullReview = String.format("%2.1f | %s", rating, text);
            locationReviews.computeIfAbsent(currentLocation, k -> new ArrayList<>()).add(fullReview);

            // Add rating to rating map
            locationRatings.computeIfAbsent(currentLocation, k -> new ArrayList<>()).add(rating);

            // Add review to UI
            Label label = new Label(fullReview);
            label.setWrapText(true);
            reviewList.getChildren().add(label);

            // Update rating meter
            updateAverageRating(currentLocation);

            // Clear inputs
            reviewInput.clear();
            ratingCombo.setValue(10);
        }
    }


    private void loadReviewsForLocation(String location){
        if(reviewList != null){
            reviewList.getChildren().clear();
        }
        reviewList.getChildren().clear();

        List<String> reviews = locationReviews.getOrDefault(location, new ArrayList<>());

        for(String review : reviews){
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







}





