package org.jmc.pintfinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

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

    @FXML
    public void initialize() {
        URL mapUrl = getClass().getResource("/map.html");
        if (mapUrl != null) {
            System.out.println(" Loading: " + mapUrl.toExternalForm());
            mapView.getEngine().load(mapUrl.toExternalForm());
        } else {
            System.out.println(" map.html not found in resources!");
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



}





