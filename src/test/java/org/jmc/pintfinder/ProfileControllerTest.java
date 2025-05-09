package org.jmc.pintfinder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileControllerTest {

    @Test
    public void testWrapTextSmart_basicWrap() {
        String input = "This is a simple sentence that should wrap after a few words.";
        int maxLineLength = 20;

        String wrapped = ProfileController.wrapTextSmart(input, maxLineLength);

        // Check that no line is longer than maxLineLength
        for (String line : wrapped.split("\n")) {
            assertTrue(line.length() <= maxLineLength,
                    "Line too long: '" + line + "'");
        }
    }

    @Test
    public void testWrapTextSmart_emptyInput() {
        assertEquals("", ProfileController.wrapTextSmart("", 10));
    }

    @Test
    public void testWrapTextSmart_nullInput() {
        assertEquals("", ProfileController.wrapTextSmart(null, 10));
    }
}
