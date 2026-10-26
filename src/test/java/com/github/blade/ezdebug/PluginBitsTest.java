package com.github.blade.ezdebug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The API version checks and the potion's clock. */
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
    void theLinkToolNeedsApi13() {
        assertTrue(EzDebugPlugin.hasLinkTool("1.3"));
        assertTrue(EzDebugPlugin.hasLinkTool("1.12"));
        assertFalse(EzDebugPlugin.hasLinkTool("1.2"), "2.2 has no link tool: EZdebug+ runs without tool vision");
        assertFalse(EzDebugPlugin.hasLinkTool("2.0"));
        assertFalse(EzDebugPlugin.hasLinkTool("odd"));
    }

    @Test
    void theClockReadsMinutesAndSeconds() {
        assertEquals("8:00", DebugPotion.clock(9600));
        assertEquals("0:30", DebugPotion.clock(600));
        assertEquals("1:05", DebugPotion.clock(1300));
    }
}
