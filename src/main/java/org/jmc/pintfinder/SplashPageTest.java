package org.jmc.pintfinder;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;


public class SplashPageTest extends javafx.application.Application {

    public static Firestore fstore;
    public static FirebaseAuth fauth;

    /**
     * load the default login screen
     * @param stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {

        initializeFirebase(); // Initialize Firebase before loading the UI

        FXMLLoader fxmlLoader = new FXMLLoader(SplashPageTest.class.getResource("/org/jmc/pintfinder/splashPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),400, 410);
        stage.getIcons().add(new Image("file:src/main/resources/img/PintFinder_Logo.png"));//sets favicon
        stage.setTitle("PintFinder's Home Page!");
        stage.setScene(scene);
        stage.show();


    }

    /**
     * enables the firebase to link to the application
     * key.json is stored in the gitignore file
     * @throws IOException
     */
    private void initializeFirebase() throws IOException {
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("key.json");

        if (serviceAccount == null) {
            throw new IOException("Firebase service account key file not found!");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        fstore = FirestoreClient.getFirestore();
        fauth = FirebaseAuth.getInstance();

        System.out.println("Firebase initialized successfully!");
    }

    public static void main(String[] args) {
        launch();
    }
}
