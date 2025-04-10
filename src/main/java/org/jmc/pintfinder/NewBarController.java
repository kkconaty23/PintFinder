package org.jmc.pintfinder;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;



public class NewBarController {
    @FXML
    private TextField googleLink;

    @FXML
    private Button submitBtn;

    @FXML
    public void submitBtnAction(ActionEvent event) {
        String link = googleLink.getText();
        if (link != null && !link.isEmpty()) {
            String[] details = parseGoogleMapsLink(link);
            if (details != null) {
                String name = details[0];
                String latitude = details[1];
                String longitude = details[2];
                System.out.println("Name: " + name);
                System.out.println("Latitude: " + latitude);
                System.out.println("Longitude: " + longitude);
            } else {
                System.out.println("Invalid Google Maps link.");
            }
        } else {
            System.out.println("Link is empty.");
        }
        googleLink.setText("");
    }

    private String[] parseGoogleMapsLink(String link) {
        try {
            // Resolve shortened URL to full URL
            String resolvedLink = resolveShortenedUrl(link);
            if (resolvedLink == null) {
                System.err.println("Failed to resolve shortened URL.");
//                return null;
                resolvedLink = link;
            }

            // Parse the resolved URL
            String[] parts = resolvedLink.split("/@");
            if (parts.length > 1) {
                String namePart = parts[0].substring(parts[0].lastIndexOf('/') + 1).replace("+", " ");
                String[] coordinates = parts[1].split(",|/");
                if (coordinates.length >= 2) {
                    String latitude = coordinates[0];
                    String longitude = coordinates[1];
                    return new String[]{namePart, latitude, longitude};
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing link: " + e.getMessage());
        }
        return null;
    }

    private String resolveShortenedUrl(String shortenedUrl) {
        try {
            java.net.URL url = new java.net.URL(shortenedUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            String location = connection.getHeaderField("Location");
            connection.disconnect();
            return location;
        } catch (Exception e) {
            System.err.println("Error resolving shortened URL: " + e.getMessage());
            return null;
        }
    }
//https://maps.app.goo.gl/VbRf1vsmS1u2VZoZ6
}
