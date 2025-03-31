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
     * added a create account button allows a new window to open where the user can be added to the firebase
     * stores their name, email, and password
     * @param event
     */
    @FXML
    private void createAcctBtnClick(ActionEvent event) {
        String email = emailID.getText();
        String password = passwordID.getText();
        String fname = firstName.getText();
        String lname = lastName.getText();

        if (!checkBox.isSelected()) {
            warningLabel.setText("Must Answer");
            return;
        }

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

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error creating user: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * method for transferring a registered user from the login page to the profile page
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
    void makeActBtnClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("createAccount.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Create Account");
        stage.setScene(new Scene(root));
        stage.show();
    }



}





