package com.github.blade.ezdebug.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Dotted lines and squares for debug vision (0.4.0): the links between
 * devices, and a weather station's area. Pure: points are in world
 * coordinates.
 */
public final class Dots {
    /** Blocks between specks along a link. */
    public static final double LINK_STEP = 0.5;
    /** The longest link drawn, in blocks: longer ones are drawn up to this from their start. */
    public static final double LONGEST_LINK = 48.0;
    /** Blocks between specks along a station's square. */
    public static final double SQUARE_STEP = 2.0;

    private Dots() {}

    public record Point(double x, double y, double z) {}

    /** Specks from one block's middle toward another's, every {@link #LINK_STEP}, ends left out. */
    public static List<Point> line(double ax, double ay, double az, double bx, double by, double bz) {
        double dx = bx - ax, dy = by - ay, dz = bz - az;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        List<Point> points = new ArrayList<>();
        if (!(length > LINK_STEP)) return points;
        double drawn = Math.min(length, LONGEST_LINK);
        for (double along = LINK_STEP; along < drawn - LINK_STEP / 2; along += LINK_STEP) {
            double t = along / length;
            points.add(new Point(ax + dx * t, ay + dy * t, az + dz * t));
        }
        return points;
    }

    /**
     * The edge of a square area {@code radius} blocks each way round a
     * block's middle, at height {@code y}, every {@link #SQUARE_STEP}: only
     * the stretches within {@code near} blocks of a viewer at (vx, vz), so
     * a big area costs nothing where no one is looking.
     */
    public static List<Point> square(double cx, double cz, double y, double radius, double vx, double vz, double near) {
        List<Point> points = new ArrayList<>();
        if (!(radius > 0.0)) return points;
        double low = -radius, high = radius;
        int steps = (int) Math.ceil(2 * radius / SQUARE_STEP);
        for (int i = 0; i <= steps; i++) {
            double t = Math.min(high, low + i * SQUARE_STEP);
            addNear(points, cx + t, y, cz + low, vx, vz, near);
            addNear(points, cx + t, y, cz + high, vx, vz, near);
            if (i == 0 || i == steps) continue;
            addNear(points, cx + low, y, cz + t, vx, vz, near);
            addNear(points, cx + high, y, cz + t, vx, vz, near);
        }
        return points;
    }

    private static void addNear(List<Point> points, double x, double y, double z, double vx, double vz, double near) {
        double dx = x - vx, dz = z - vz;
        if (dx * dx + dz * dz <= near * near) points.add(new Point(x, y, z));
    }
}
