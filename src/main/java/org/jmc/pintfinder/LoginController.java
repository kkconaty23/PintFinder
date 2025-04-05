package org.jmc.pintfinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

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
    void moveToCreateAccountPage(ActionEvent event) {
 try {

 FXMLLoader fxmlCreateAccountLoader = new FXMLLoader(Login.class.getResource("createAccount.fxml"));

 Scene createAccountScene = new Scene(fxmlCreateAccountLoader.load(), 397, 600);

 Stage createAccountStage = new Stage();
 createAccountStage.setTitle("Create Account Page");
 createAccountStage.setScene(createAccountScene);
 createAccountStage.show();


 Stage currentStage = (Stage) createAcctBtn.getScene().getWindow();
 currentStage.close();
 } catch (Exception e) {
 e.printStackTrace();
 Alert alert = new Alert(Alert.AlertType.ERROR);
 alert.setTitle("Error");
 alert.setHeaderText("Failed to load Create Account Page");
 alert.setContentText("An error occurred while trying to load the Create Account page.");
 alert.showAndWait();
 }}


    /**
     * added a create account button allows a new window to open where the user can be added to the firebase
     *
     * @param event
     */
    /**
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
*/
    public void signInBtnClick(ActionEvent actionEvent) throws IOException {
        //once we make the home page and profile pages allow this to send the user to their homepage.

        FXMLLoader fxmlhomePageLoader = new FXMLLoader(Login.class.getResource("homePage.fxml"));

        Scene HomePageScene = new Scene(fxmlhomePageLoader.load(),1200, 740);

        Stage HomePageStage = new Stage();
        HomePageStage.setTitle("Home Page");
        HomePageStage.setScene(HomePageScene);
        HomePageStage.show();


        Stage currentStage = (Stage) signInBtn.getScene().getWindow();
        currentStage.close();

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





