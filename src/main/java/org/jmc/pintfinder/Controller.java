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
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Controller {
    @FXML
    public Button createAcctBtn;

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





