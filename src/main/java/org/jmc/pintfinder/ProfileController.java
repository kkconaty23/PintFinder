package org.jmc.pintfinder;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfileController {

    @FXML private ImageView profileBackBtn;

    @FXML void bringToHomepage(MouseEvent event) throws IOException {

        FXMLLoader fxmlProfileLoader = new FXMLLoader(Login.class.getResource("homePage.fxml"));

        Scene ProfileScene = new Scene(fxmlProfileLoader.load(), 800, 600);

        Stage ProfileStage = new Stage();
        ProfileStage.setTitle("Profile Page");
        ProfileStage.setScene(ProfileScene);
        ProfileStage.show();


        Stage currentStage = (Stage) profileBackBtn.getScene().getWindow();
        currentStage.close();

    }
}
