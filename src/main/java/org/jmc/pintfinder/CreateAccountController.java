package org.jmc.pintfinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

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
 }}


    /**
     * added a create account button allows a new window to open where the user can be added to the firebase
     *
     * @param event
     */
    @FXML
    private void createAcctBtnClick(ActionEvent event) {//when a user creates account it makes them account that links to FB

        String email = emailID.getText();
        String password = passwordID.getText();
        String confirmPassword = confirmPasswordID.getText();



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

        // create a user in Firebase
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);
            //.setConfirmPassword(confirmPassword);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            System.out.println("User created successfully: " + userRecord.getUid());//testing users
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating user: " + e.getMessage());
        }
    }}

    public void signInBtnClick(ActionEvent actionEvent) {
        //once we make the home page and profile pages allow this to send the user to their homepage.

    }

    /**
     * registers the users email and password and puts them into firebase
     * @param event
     * @throws IOException
     */
    @FXML
    void makeActBtnClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateAccount.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Create Account");
        stage.setScene(new Scene(root));
        stage.show();
    }



}





