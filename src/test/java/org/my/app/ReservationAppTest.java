package org.my.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.muserver.MuServer;
import org.junit.jupiter.api.Test;

/**
 * Unit test for reservation App.
 */
class ReservationAppTest {

    /**
     */
    @Test
    void versionIsAvailable() {
        var version = MuServer.artifactVersion();
        assertTrue(MuServer.artifactVersion().contains("2.1.8"));
    }
}
