package org.jmc.pintfinder;

import org.jmc.pintfinder.LoginController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LoginControllerTest {

    @Test
    public void testControllerInstantiation() {
        LoginController controller = new LoginController();
        assertNotNull(controller);
    }
}
