package org.jmc.pintfinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    @FXML
    private Label logOutBtn;
    @FXML private ImageView profileBackBtn;
    @FXML private Label userFirstNameText;
    @FXML private Label lastBarReview;
    @FXML private Label numReviews;
    @FXML private ListView<String> pastReviews;


    @FXML
    private Label dateLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserFirstName();
        fetchCurrentUserReviews();
        try {
            loadNumReviews();
            loadLastBarReview();
            loadDate();
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gets the data from fire base when the user was created and displays it when PP is init
     * @throws FirebaseAuthException
     */
    private void loadDate() throws FirebaseAuthException {
        String uid = SessionManager.getCurrentUserUid();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        UserRecord user = auth.getUser(uid);  // Or auth.getUserByEmail(email)

        long creationTimestamp = user.getUserMetadata().getCreationTimestamp();
        Date creationDate = new Date(creationTimestamp);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
        String formattedDate = dateFormat.format(creationDate);

        dateLabel.setText(formattedDate);
    }

    private void loadNumReviews() {
        String uid = SessionManager.getCurrentUserUid();
        if (uid == null) {
            System.out.println("No user logged in.");
            return;
        }

        DatabaseReference userReviewsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("reviews");

        userReviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int reviewCount = (int) snapshot.getChildrenCount();
                Platform.runLater(() -> numReviews.setText(String.valueOf(reviewCount)));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error fetching review count: " + error.getMessage());
            }
        });
    }

    private void loadLastBarReview() {
        String uid = SessionManager.getCurrentUserUid();
        if (uid == null) {
            System.out.println("No user logged in.");
            return;
        }

        DatabaseReference userReviewsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("reviews");

        userReviewsRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    String barName = reviewSnapshot.child("barName").getValue(String.class);
//                    String text = reviewSnapshot.child("text").getValue(String.class);
//                    Double rating = reviewSnapshot.child("rating").getValue(Double.class);

                    String lastReviewText = String.format("%s", barName);
                    Platform.runLater(() -> lastBarReview.setText(lastReviewText));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error fetching last bar review: " + error.getMessage());
            }
        });
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
                    userFirstNameText.setMinWidth(175);
                    userFirstNameText.setAlignment(Pos.CENTER);
                    userFirstNameText.setLayoutX(41);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DatabaseError: " + databaseError.getMessage());
            }
        });
    }
    @FXML
    private void fetchCurrentUserReviews() {
        String uid = SessionManager.getCurrentUserUid();
        if (uid == null) {
            System.out.println("No user logged in.");
            return;
        }

        DatabaseReference userReviewsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("reviews");

        userReviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    System.out.println("User Reviews:");

                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        String barName = reviewSnapshot.child("barName").getValue(String.class);
                        String text = reviewSnapshot.child("text").getValue(String.class);
                        Double rating = reviewSnapshot.child("rating").getValue(Double.class);

                        //dynamic char limit based on list view size
                        int charLimit = (int)(pastReviews.getPrefWidth() / 7.2); // Adjust this value based on your font size and ListView width

                        // Add the review to the ListView
                        String reviewText = wrapTextSmart(String.format("%s: %.1f ★ | %s", barName, rating != null ? rating : 0, text != null ? text : "(no text)"),
                                charLimit-6);//change this number if you change the size of the list veiw

                        pastReviews.getItems().add(reviewText);
                    }
                } else {
                    System.out.println("No reviews found for this user.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error fetching user reviews: " + error.getMessage());
            }
        });
    }
    public static String wrapTextSmart(String text, int maxLineLength) {
        if (text == null || text.isEmpty()) return "";

        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split(" ");
        int currentLineLength = 0;

        for (String word : words) {
            if (currentLineLength + word.length() > maxLineLength) {
                wrapped.append("\n");
                currentLineLength = 0;
            } else if (currentLineLength != 0) {
                wrapped.append(" ");
                currentLineLength += 1;
            }

            wrapped.append(word);
            currentLineLength += word.length();
        }

        return wrapped.toString();
    }
    @FXML void bringToHomepage(MouseEvent event) throws IOException {

        FXMLLoader fxmlProfileLoader = new FXMLLoader(Login.class.getResource("homePage.fxml"));

        Scene ProfileScene = new Scene(fxmlProfileLoader.load(), 1080, 775);

        Stage ProfileStage = new Stage();
        ProfileStage.setTitle("PintFinder's Home Page");
        ProfileStage.setScene(ProfileScene);
        ProfileStage.getIcons().add(new Image("file:src/main/resources/img/PintFinder_Logo.png"));//sets favicon
        ProfileStage.setResizable(false);
        ProfileStage.show();


        Stage currentStage = (Stage) profileBackBtn.getScene().getWindow();
        currentStage.close();

    }
    @FXML
    void returnToLogin(MouseEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("You are successfully logged out");
        // Apply custom stylesheet
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/alert.css")).toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");

        alert.showAndWait();

        Stage currentStage = (Stage) logOutBtn.getScene().getWindow();
        currentStage.close();
        FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource("Login.fxml"));

        Scene loginScene = new Scene(fxmlLoader.load(), 397, 400);

        Stage loginStage = new Stage();

        loginStage.setScene(loginScene);
        loginStage.getIcons().add(new Image("file:src/main/resources/img/PintFinder_Logo.png"));//sets favicon

        loginStage.show();

    }

}
