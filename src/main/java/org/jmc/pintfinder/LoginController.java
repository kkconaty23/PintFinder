package org.jmc.pintfinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {
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
    private Button makeAcctBtn;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;


    /**
     * query database and matches emails and passwords
     * @param actionEvent
     */
    public void signInBtnClick(ActionEvent actionEvent) {
        String email = emailID.getText();
        String password = passwordID.getText();

        if (email.isEmpty() || password.isEmpty()) {
            warningLabel.setText("Please enter both email and password.");
            return;
        }

        // Reference to the "users" node in the database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Query the database to find the user by email
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the user's data
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);

                        // Check if the entered password matches the stored password (ensure password is hashed)
                        if (storedPassword.equals(password)) {
                            System.out.println("User signed in successfully!");

                            // Use Platform.runLater to ensure UI changes are on the JavaFX Application Thread
                            Platform.runLater(() -> {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
                                    Stage stage = (Stage) emailID.getScene().getWindow();
                                    Scene scene = new Scene(loader.load());

                                    // Set the scene and show the profile page
                                    stage.setScene(scene);
                                    stage.show();
                                    System.out.println("Loading profile");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    warningLabel.setText("Error loading profile.");
                                }
                            });

                        } else {
                            // Incorrect password
                            warningLabel.setText("Invalid password.");
                        }
                    }
                } else {
                    // User not found
                    warningLabel.setText("User not found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                warningLabel.setText("Error querying database: " + databaseError.getMessage());
            }
        });
    }


    /**
     * loads the create account page
     * @param event
     * @throws IOException
     */
    @FXML
    void moveToCreateAccountPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("createAccount.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Create Account");
        stage.setScene(new Scene(root));
        stage.show();
    }



}





