package org.jmc.pintfinder;

import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SplashController {
        @FXML
     private ImageView sign;

        @FXML
        private ImageView pint1;

        @FXML
        private ImageView pint2;
    TranslateTransition tt1 = new TranslateTransition(Duration.seconds(2),pint1);
        TranslateTransition tt2 = new TranslateTransition(Duration.seconds(2),sign);
    public void initialize() {

        sign.setVisible(true);
        sign.setOpacity(1);
        tt2.setToY(0);
        TranslateTransition tt2 = new TranslateTransition(Duration.seconds(2), sign);
        tt2.setFromY(-200);
        tt2.setToY(0);
        tt2.setCycleCount(1);
        tt2.setAutoReverse(false);
        tt2.play();

        pint1.setVisible(true);
        pint1.setOpacity(1.0);


        pint1.setTranslateX(0);

        tt1 = new TranslateTransition(Duration.seconds(2), pint1);
        tt1.setFromX(-100);
        tt1.setToX(275);
        tt1.setCycleCount(1);
        tt1.setAutoReverse(false);


        tt1.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(3));  // Duration should match the animation duration
        pause.setOnFinished(event -> {
            try {

                FXMLLoader fxmlLoginLoader = new FXMLLoader(Login.class.getResource("login.fxml"));

                Scene LoginScene = new Scene(fxmlLoginLoader.load(), 397, 400);

                Stage LoginStage = new Stage();
                LoginStage.setTitle("Login Page");
                LoginStage.setScene(LoginScene);
                LoginStage.show();


                Stage currentStage = (Stage) pint1.getScene().getWindow();
                currentStage.close();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to load Login Page");
                alert.setContentText("An error occurred while trying to load the Login page.");
                alert.showAndWait();
            }
        }); pause.play();
    }}







