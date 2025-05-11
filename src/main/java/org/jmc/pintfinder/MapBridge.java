package org.jmc.pintfinder;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;





/**
 * A bridge class used to facilitate communication between JavaScript in the WebView
 * and the JavaFX backend. Supports updating UI components based on bar selection events.
 */
public class MapBridge {
    private Consumer<String> onTitleChange;
    private Consumer<String> onDescriptionChange;
    private Consumer<String> onLocationSwitch;


    /**
     * Sets the action to perform when a bar's title is updated.
     *
     * @param onTitleChange a Consumer that accepts the new title string
     */
    public void setOnTitleChange(Consumer<String> onTitleChange) {
        this.onTitleChange = onTitleChange;
    }


    /**
     * Sets the action to perform when a bar's description is updated.
     *
     * @param onDescriptionChange a Consumer that accepts the new description string
     */
    public void setOnDescriptionChange(Consumer<String> onDescriptionChange) {
        this.onDescriptionChange = onDescriptionChange;
    }

    /**
     * Sets the action to perform when the selected location (bar) changes.
     *
     * @param onLocationSwitch a Consumer that accepts the bar name
     */
    public void setOnLocationSwitch(Consumer<String> onLocationSwitch) {
        this.onLocationSwitch = onLocationSwitch;
    }

    public void updateLocation(String title, String description) {
        if (onTitleChange != null) { onTitleChange.accept(title); }
        if (onDescriptionChange != null) { onDescriptionChange.accept(description); }
        if (onLocationSwitch != null) { onLocationSwitch.accept(title); }
    }
    // Replace the getUserLocation method in your MapBridge class with this implementation
    public void getUserLocation() {
        System.out.println("Getting user location...");

        // Use CompletableFuture to handle the asynchronous operation
        CompletableFuture.supplyAsync(() -> {
            try {
                // Try multiple geolocation services for better reliability

                // Method 1: Try using ipinfo.io (more reliable)
                String ipLocation = getLocationFromIpInfo();
                if (ipLocation != null) {
                    System.out.println("Location from ipinfo.io: " + ipLocation);
                    return ipLocation;
                }

                // Method 2: Fallback to ip-api.com
                String ipApiLocation = getLocationFromIpApi();
                if (ipApiLocation != null) {
                    System.out.println("Location from ip-api.com: " + ipApiLocation);
                    return ipApiLocation;
                }

                // Method 3: Additional fallback if needed
                String geoPluginLocation = getLocationFromGeoPlugin();
                if (geoPluginLocation != null) {
                    System.out.println("Location from geoplugin: " + geoPluginLocation);
                    return geoPluginLocation;
                }

                // All methods failed, return null
                return null;
            } catch (Exception e) {
                System.err.println("Error in geolocation: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }).thenAccept(locationStr -> {
            if (locationStr != null) {
                try {
                    // Parse the "latitude,longitude" string
                    String[] parts = locationStr.split(",");
                    double latitude = Double.parseDouble(parts[0].trim());
                    double longitude = Double.parseDouble(parts[1].trim());

                    // Send location back to JavaScript
                    sendLocationToJavaScript(latitude, longitude);
                } catch (Exception e) {
                    System.err.println("Error parsing location: " + e.getMessage());
                    // Fall back to default location
                    sendLocationToJavaScript(40.7528, -73.4265);
                }
            } else {
                // Fall back to default location
                System.out.println("Using default location");
                sendLocationToJavaScript(40.7528, -73.4265);
            }
        });
    }

    // Method to get location from ipinfo.io
    private String getLocationFromIpInfo() {
        try {
            URL url = new URL("https://ipinfo.io/json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            if (status == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("loc")) {
                    return jsonResponse.getString("loc"); // Format: "latitude,longitude"
                }
            } else {
                System.out.println("ipinfo.io returned status: " + status);
            }
        } catch (Exception e) {
            System.err.println("Error fetching location from ipinfo.io: " + e.getMessage());
        }
        return null;
    }

    // Method to get location from ip-api.com
    private String getLocationFromIpApi() {
        try {
            URL url = new URL("http://ip-api.com/json/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            if (status == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("lat") && jsonResponse.has("lon")) {
                    double lat = jsonResponse.getDouble("lat");
                    double lon = jsonResponse.getDouble("lon");
                    return lat + "," + lon;
                }
            } else {
                System.out.println("ip-api.com returned status: " + status);
            }
        } catch (Exception e) {
            System.err.println("Error fetching location from ip-api.com: " + e.getMessage());
        }
        return null;
    }

    // Method to get location from geoplugin.net
    private String getLocationFromGeoPlugin() {
        try {
            URL url = new URL("http://www.geoplugin.net/json.gp");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            if (status == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("geoplugin_latitude") && jsonResponse.has("geoplugin_longitude")) {
                    double lat = jsonResponse.getDouble("geoplugin_latitude");
                    double lon = jsonResponse.getDouble("geoplugin_longitude");
                    return lat + "," + lon;
                }
            } else {
                System.out.println("geoplugin.net returned status: " + status);
            }
        } catch (Exception e) {
            System.err.println("Error fetching location from geoplugin.net: " + e.getMessage());
        }
        return null;
    }

    // Helper method to send location to JavaScript
    private void sendLocationToJavaScript(double latitude, double longitude) {
        if (webEngine != null) {
            Platform.runLater(() -> {
                try {
                    String script = "if(window.tempGeolocationCallback) {" +
                            "window.tempGeolocationCallback({" +
                            "    coords: {" +
                            "        latitude: " + latitude + "," +
                            "        longitude: " + longitude + "," +
                            "        accuracy: 1000" +  // Reasonable accuracy for IP-based location
                            "    }" +
                            "});" +
                            "console.log('Location sent to JavaScript: " + latitude + ", " + longitude + "');" +
                            "}";
                    webEngine.executeScript(script);
                    System.out.println("Location sent to JavaScript: " + latitude + ", " + longitude);
                } catch (Exception e) {
                    System.err.println("Error sending location to JavaScript: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            System.err.println("WebEngine is null, cannot send location to JavaScript");
        }
    }

    // Reference to WebEngine (needs to be set from controller)
    private WebEngine webEngine;

    public void setWebEngine(WebEngine engine) {
        this.webEngine = engine;
    }
}
