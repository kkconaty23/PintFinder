package org.jmc.pintfinder;

import com.google.firebase.database.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private ImageView profileBackBtn;
    @FXML private Label userFirstNameText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserFirstName();
    }

    private void loadUserFirstName() {
        String uid = SessionManager.getCurrentUserUid();
        if (uid == null){
            System.out.println("No user logged in!");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("firstName");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstName = dataSnapshot.getValue(String.class);
                if (firstName != null){
                    Platform.runLater(() -> userFirstNameText.setText(firstName));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DatabaseError: " + databaseError.getMessage());
            }
        });
    }

    @FXML void bringToHomepage(MouseEvent event) throws IOException {

        FXMLLoader fxmlProfileLoader = new FXMLLoader(Login.class.getResource("homePage.fxml"));

        Scene ProfileScene = new Scene(fxmlProfileLoader.load(), 1080, 775);

        Stage ProfileStage = new Stage();
        ProfileStage.setTitle("Profile Page");
        ProfileStage.setScene(ProfileScene);
        ProfileStage.getIcons().add(new Image("file:src/main/resources/img/PintFinder_Logo.png"));//sets favicon
        ProfileStage.setResizable(false);
        ProfileStage.show();


        Stage currentStage = (Stage) profileBackBtn.getScene().getWindow();
        currentStage.close();

    }

}
