package org.jmc.pintfinder;

import java.util.function.Consumer;



public class MapBridge {
    private Consumer<String> onTitleChange;
    private Consumer<String> onDescriptionChange;
    private Consumer<String> onLocationSwitch;



    public void setOnTitleChange(Consumer<String> onTitleChange) {
        this.onTitleChange = onTitleChange;
    }



    public void setOnDescriptionChange(Consumer<String> onDescriptionChange) {
        this.onDescriptionChange = onDescriptionChange;
    }


    public void setOnLocationSwitch(Consumer<String> onLocationSwitch) {
        this.onLocationSwitch = onLocationSwitch;
    }

    public void updateLocation(String title, String description) {
        if (onTitleChange != null) { onTitleChange.accept(title); }
        if (onDescriptionChange != null) { onDescriptionChange.accept(description); }
        if (onLocationSwitch != null) { onLocationSwitch.accept(title); }
    }
}
