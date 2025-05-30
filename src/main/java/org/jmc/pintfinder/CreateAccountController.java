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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *  Controller Class for the createAccount.fxml
 *  Handles form validation, account creation, and navigation to the login page.
 */
public class CreateAccountController {

    // Buttons
    @FXML public Button loginBtn;
    @FXML private Button signUpBtn;

    // CheckBox
    @FXML private CheckBox checkBox;

    // TextFields & PasswordFields
    @FXML private TextField lastNameID;
    @FXML private TextField firstNameID;
    @FXML private TextField emailID;
    @FXML private PasswordField confirmPasswordID;
    @FXML private PasswordField passwordID;

    /**
     * Initializes the form and adds listeners to enable the sign-up button
     * only when all fields are filled out
     */
    public void initialize() {
        signUpBtn.setOpacity(.3);
        signUpBtn.setDisable(true);
        emailID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        passwordID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        confirmPasswordID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        firstNameID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        lastNameID.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
    }

    /**
     * Enables or disables the sign-up button based on whether all required fields are filled.
     */
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
    /**
     * Navigates the user back to the login page.
     *
     * @param event The action event triggered by clicking the "Login" button.
     */
    @FXML
    void moveToLoginPage(ActionEvent event) {
        try {

            FXMLLoader fxmlLoginLoader = new FXMLLoader(Login.class.getResource("login.fxml"));

            Scene LoginScene = new Scene(fxmlLoginLoader.load(), 397, 400);

            Stage LoginStage = new Stage();
            LoginStage.setTitle("Login Page");
            LoginStage.setScene(LoginScene);
            LoginStage.getIcons().add(new Image("file:src/main/resources/img/PintFinder_Logo.png"));//sets favicon
            LoginStage.setResizable(false);
            LoginStage.show();


            Stage currentStage = (Stage) loginBtn.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Login Page");
            alert.setContentText("An error occurred while trying to load the Login page.");
            // Apply custom stylesheet
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/alert.css")).toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");

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
            // Apply custom stylesheet
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/alert.css")).toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");
            alert.showAndWait();
        }
        else if(!passwordID.getText().equals(confirmPasswordID.getText())){
            Alert alert1 = new Alert(Alert.AlertType.WARNING);
            alert1.setTitle("Error");
            alert1.setHeaderText("Password Mismatch");
            //warningLabel.setText("Passwords do not match");
            // Apply custom stylesheet
            alert1.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/alert.css")).toExternalForm());
            alert1.getDialogPane().getStyleClass().add("dialog-pane");
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
            SessionManager.setCurrentUserUid(userRecord.getUid()); //Assign ID (sessionManager.java)


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
            // Apply custom stylesheet
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/alert.css")).toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");
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
            // Apply custom stylesheet
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/alert.css")).toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");

            alert.showAndWait();
        }
    }
}



}








