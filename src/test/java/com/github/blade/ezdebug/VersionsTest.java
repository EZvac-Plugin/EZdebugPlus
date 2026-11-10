package com.github.blade.ezdebug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Which EZvac offers what EZdebug+ 0.4.0 draws. */
class VersionsTest {
    @Test
    void roomsNeedApi110AndStationsApi17() {
        assertFalse(Spaces.offered("1.9"));
        assertTrue(Spaces.offered("1.10"), "1.10 is newer than 1.9, not older");
        assertTrue(Spaces.offered("1.12"));
        assertFalse(Spaces.offered("2.0"));
        assertFalse(Spaces.offered("odd"));
        assertTrue(DebugVision.atLeast("1.7", 7));
        assertFalse(DebugVision.atLeast("1.6", 7));
        assertTrue(DebugVision.atLeast("1.10", 7));
    }
}
