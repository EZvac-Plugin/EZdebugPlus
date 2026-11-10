package com.github.blade.ezdebug;

import com.github.blade.hvac.api.EzvacApi;
import com.github.blade.hvac.api.Position;
import com.github.blade.hvac.api.SystemSpace;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rooms as EZvac sees them (EZvac API 1.10, BETA 1.0 Pilot 5): the edge of
 * the space each system's air fills, dotted in near the viewer, and the
 * openings it leaks through, which {@link DebugVision} has outlined in a
 * glowing red. Kept in a class of its own, only loaded on an EZvac that has
 * {@code systemSpace}, so EZdebug+ still runs on older ones.
 */
final class Spaces {
    /** The most edge specks drawn for one player at a time, nearest first. */
    static final int MOST_EDGE_SPECKS = 700;
    /** How near the player a room's edge is dotted in, in blocks. */
    static final double EDGE_RADIUS = 24.0;
    private static final int EDGE = 0xF2F2F2;

    private Spaces() {}

    /** API 1.10 or newer, within major version 1. */
    static boolean offered(String apiVersion) {
        String[] parts = apiVersion.split("\\.");
        try {
            return parts.length >= 2 && Integer.parseInt(parts[0]) == 1 && Integer.parseInt(parts[1]) >= 10;
        } catch (NumberFormatException unknown) {
            return false;
        }
    }

    /**
     * For each viewer, the systems they see: their edges are dotted in, and
     * where each system leaks is returned, with who sees it, for the glowing
     * outlines.
     */
    static Map<Position, List<Player>> draw(EzvacApi ezvac, World world, Map<Player, Collection<String>> seen, long tick) {
        Map<String, Optional<SystemSpace>> spaces = new HashMap<>();
        Map<Position, List<Player>> leaks = new HashMap<>();
        for (Map.Entry<Player, Collection<String>> entry : seen.entrySet()) {
            Player player = entry.getKey();
            List<Position> edge = new ArrayList<>();
            for (String system : entry.getValue()) {
                Optional<SystemSpace> space = spaces.computeIfAbsent(system, key -> ezvac.systemSpace(world, key));
                if (space.isEmpty()) continue;
                edge.addAll(space.get().edge());
                for (Position opening : space.get().openings())
                    leaks.computeIfAbsent(opening, ignored -> new ArrayList<>()).add(player);
            }
            edge(player, edge, tick);
        }
        return leaks;
    }

    /** The specks of the edges nearest the player, faint, twinkling farther out so a big room isn't a fog. */
    private static void edge(Player player, List<Position> edge, long tick) {
        double px = player.getLocation().getX(), py = player.getLocation().getY(), pz = player.getLocation().getZ();
        List<double[]> near = new ArrayList<>();
        for (Position cell : edge) {
            double dx = cell.x() + 0.5 - px, dy = cell.y() + 0.5 - py, dz = cell.z() + 0.5 - pz;
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance <= EDGE_RADIUS * EDGE_RADIUS) near.add(new double[]{distance, cell.x(), cell.y(), cell.z()});
        }
        near.sort((a, b) -> Double.compare(a[0], b[0]));
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(EDGE), 0.55f);
        for (int i = 0; i < Math.min(MOST_EDGE_SPECKS, near.size()); i++) {
            double[] cell = near.get(i);
            if (i > MOST_EDGE_SPECKS / 2 && ((long) (cell[1] + cell[2] + cell[3]) + tick) % 2 != 0) continue;
            player.spawnParticle(Particle.DUST, cell[1] + 0.5, cell[2] + 0.5, cell[3] + 0.5, 1, 0.0, 0.0, 0.0, 0.0, dust);
        }
    }
}
