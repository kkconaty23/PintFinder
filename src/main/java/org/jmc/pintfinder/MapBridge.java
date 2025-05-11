package org.jmc.pintfinder;

import java.util.function.Consumer;


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
}
