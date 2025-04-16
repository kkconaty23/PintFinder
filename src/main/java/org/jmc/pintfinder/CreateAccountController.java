package org.jmc.pintfinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateAccountController {
    @FXML
    public Button loginBtn;

    @FXML
    private Label welcomeText;

    @FXML
    private Button signUpBtn;

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
    private TextField lastNameID;

    @FXML
    private TextField firstNameID;

    @FXML
    private PasswordField confirmPasswordID;

    public void initialize() {
        signUpBtn.setOpacity(.3);
        signUpBtn.setDisable(true);
        emailID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        passwordID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        confirmPasswordID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        firstNameID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        lastNameID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
    }

    private void checkFields() {
        boolean fieldsFilled = !emailID.getText().isEmpty() &&
                !passwordID.getText().isEmpty() &&
                !confirmPasswordID.getText().isEmpty() &&
                !firstNameID.getText().isEmpty() &&
                !lastNameID.getText().isEmpty();


        if (fieldsFilled) {
            signUpBtn.setOpacity(1);
            signUpBtn.setDisable(false);
        } else {
            signUpBtn.setOpacity(0.3);
            signUpBtn.setDisable(true);
        }
    }

    @FXML
    void moveToLoginPage(ActionEvent event) {
        try {

            FXMLLoader fxmlLoginLoader = new FXMLLoader(Login.class.getResource("login.fxml"));

            Scene LoginScene = new Scene(fxmlLoginLoader.load(), 397, 400);

            Stage LoginStage = new Stage();
            LoginStage.setTitle("Login Page");
            LoginStage.setScene(LoginScene);
            LoginStage.getIcons().add(new Image("file:src/main/resources/img/PintFinder_Logo.png"));//sets favicon
            LoginStage.show();


            Stage currentStage = (Stage) loginBtn.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Login Page");
            alert.setContentText("An error occurred while trying to load the Login page.");
            alert.showAndWait();
        }
    }


    /**
     * added a create account button allows a new window to open where the user can be added to the firebase
     *
     * @param event
     */
    @FXML
    private void createAcctBtnClick(ActionEvent event) {
        String email = emailID.getText();
        String password = passwordID.getText();
        String fname = firstNameID.getText();
        String lname = lastNameID.getText();



        if(! checkBox.isSelected()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            //warningLabel.setText("Must Answer");
            alert.setTitle("Warning");
            alert.setHeaderText("You Must be 21 or older");
            alert.showAndWait();
        }
        else if(!passwordID.getText().equals(confirmPasswordID.getText())){
            Alert alert1 = new Alert(Alert.AlertType.WARNING);
            alert1.setTitle("Error");
            alert1.setHeaderText("Password Mismatch");
            //warningLabel.setText("Passwords do not match");
            alert1.showAndWait();
        }
        else{


        try {
            // Create user in Firebase Authentication
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);


            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            System.out.println("User created successfully: " + userRecord.getUid());

            // Store user data in Firebase Realtime Database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

            // Create a user object
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("password", password);
            userData.put("firstName", fname);
            userData.put("lastName", lname);

            databaseReference.child(userRecord.getUid()).setValueAsync(userData);// storing user data into the database

            // Show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Account Created Successfully!");
            alert.showAndWait();

            Stage currentStage = (Stage) loginBtn.getScene().getWindow();
            currentStage.close();
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource("Login.fxml"));

            Scene loginScene = new Scene(fxmlLoader.load(), 397, 400);

            Stage loginStage = new Stage();

            loginStage.setScene(loginScene);
            loginStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error creating user: " + e.getMessage());
            alert.showAndWait();
        }
    }


}}








