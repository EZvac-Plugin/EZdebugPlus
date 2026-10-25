package com.github.blade.ezdebug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The API version check and the potion's clock. */
class PluginBitsTest {
    @Test
    void needsApi12OrNewerWithinMajorVersionOne() {
        assertTrue(EzDebugPlugin.supported("1.2"));
        assertTrue(EzDebugPlugin.supported("1.10"));
        assertFalse(EzDebugPlugin.supported("1.1"));
        assertFalse(EzDebugPlugin.supported("1.0"));
        assertFalse(EzDebugPlugin.supported("2.0"), "a new major version may break us");
        assertFalse(EzDebugPlugin.supported("odd"));
    }

    @Test
    void theClockReadsMinutesAndSeconds() {
        assertEquals("8:00", DebugPotion.clock(9600));
        assertEquals("0:30", DebugPotion.clock(600));
        assertEquals("1:05", DebugPotion.clock(1300));
    }
}
