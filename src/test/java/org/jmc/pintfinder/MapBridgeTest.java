package org.jmc.pintfinder;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MapBridgeTest {

    @Test
    void testSetOnTitleChange() {
        MapBridge mapBridge = new MapBridge();
        AtomicReference<String> result = new AtomicReference<>();

        mapBridge.setOnTitleChange(result::set);

        // Simulate an internal call (like what updateLocation would do)
        mapBridge.updateLocation("Test Title", "Dummy Description");

        assertEquals("Test Title", result.get());
    }
}
