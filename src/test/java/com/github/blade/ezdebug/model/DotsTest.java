package com.github.blade.ezdebug.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The dotted links and station squares (0.4.0). */
class DotsTest {
    @Test
    void aLinkIsDottedBetweenItsEndsWithoutThem() {
        List<Dots.Point> points = Dots.line(0, 0, 0, 5, 0, 0);
        assertEquals(9, points.size(), "every half block between, the ends left out");
        assertEquals(0.5, points.get(0).x(), 1e-9);
        assertEquals(4.5, points.get(points.size() - 1).x(), 1e-9);
        assertTrue(Dots.line(0, 0, 0, 0.3, 0, 0).isEmpty(), "two blocks side by side need no line");
        List<Dots.Point> far = Dots.line(0, 0, 0, 500, 0, 0);
        assertTrue(far.get(far.size() - 1).x() <= Dots.LONGEST_LINK, "a long link is cut short");
    }

    @Test
    void aStationsSquareIsDrawnOnlyNearTheViewer() {
        List<Dots.Point> whole = Dots.square(0, 0, 70, 10, 0, 0, 100);
        assertEquals(40, whole.size(), "a 20-block square, a speck every 2 blocks round it");
        assertTrue(whole.stream().allMatch(p -> Math.max(Math.abs(p.x()), Math.abs(p.z())) == 10.0), "all on its edge");
        assertTrue(whole.stream().allMatch(p -> p.y() == 70));
        List<Dots.Point> near = Dots.square(0, 0, 70, 10, 10, 0, 4);
        assertFalse(near.isEmpty());
        assertTrue(near.size() < 10, "only the stretch beside the viewer");
        assertTrue(near.stream().allMatch(p -> Math.hypot(p.x() - 10, p.z()) <= 4), near.toString());
        assertTrue(Dots.square(0, 0, 70, 10, 200, 200, 30).isEmpty(), "nothing where no one is looking");
        assertTrue(Dots.square(0, 0, 70, 0, 0, 0, 30).isEmpty());
    }
}
