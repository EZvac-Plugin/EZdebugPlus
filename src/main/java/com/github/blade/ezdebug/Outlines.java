package com.github.blade.ezdebug;

import com.github.blade.hvac.api.Position;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Glowing outlines around equipment, seen only by players with debug vision.
 * Each is a block display: a copy of the dispenser, a hair larger, glowing in
 * the colour of what it's doing. (Signs can't glow: see SignFrame.) Hidden from everyone by default and shown
 * to each debug player who should see it. Never saved with the world.
 * Main thread only.
 */
final class Outlines {
    /** Slightly larger than the block, so the copy never flickers against it. */
    private static final float SCALE = 1.02f;

    private static final class Outline {
        final BlockDisplay display;
        final Set<UUID> viewers = new HashSet<>();
        int color;

        Outline(BlockDisplay display, int color) {
            this.display = display;
            this.color = color;
        }
    }

    /** What one refresh wants outlined: a colour, and who sees it. */
    record Wanted(int color, Set<Player> viewers) {}

    private final Plugin plugin;
    private final Map<Position, Outline> outlines = new HashMap<>();

    Outlines(Plugin plugin) {
        this.plugin = plugin;
    }

    /** Makes the outlines match this refresh: new ones go up, colours change, stale ones come down. */
    void show(Map<Position, Wanted> wanted) {
        for (Iterator<Map.Entry<Position, Outline>> it = outlines.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Position, Outline> entry = it.next();
            if (!wanted.containsKey(entry.getKey()) || !entry.getValue().display.isValid()) {
                entry.getValue().display.remove();
                it.remove();
            }
        }
        for (Map.Entry<Position, Wanted> entry : wanted.entrySet()) {
            Outline outline = outlines.get(entry.getKey());
            if (outline == null) {
                BlockDisplay display = spawn(entry.getKey(), entry.getValue().color());
                if (display == null) continue;
                outline = new Outline(display, entry.getValue().color());
                outlines.put(entry.getKey(), outline);
            } else if (outline.color != entry.getValue().color()) {
                outline.color = entry.getValue().color();
                outline.display.setGlowColorOverride(Color.fromRGB(outline.color));
            }
            reveal(outline, entry.getValue().viewers());
        }
    }

    private void reveal(Outline outline, Set<Player> viewers) {
        Set<UUID> now = new HashSet<>();
        for (Player player : viewers) {
            now.add(player.getUniqueId());
            if (outline.viewers.add(player.getUniqueId())) player.showEntity(plugin, outline.display);
        }
        for (Iterator<UUID> it = outline.viewers.iterator(); it.hasNext(); ) {
            UUID viewer = it.next();
            if (now.contains(viewer)) continue;
            Player player = plugin.getServer().getPlayer(viewer);
            if (player != null) player.hideEntity(plugin, outline.display);
            it.remove();
        }
    }

    /** A copy of the block at this spot, a hair larger, glowing; null when its world or chunk isn't loaded. */
    private BlockDisplay spawn(Position at, int color) {
        World world = plugin.getServer().getWorld(at.world());
        if (world == null || !world.isChunkLoaded(at.x() >> 4, at.z() >> 4)) return null;
        Block block = world.getBlockAt(at.x(), at.y(), at.z());
        if (block.getType().isAir()) return null;
        float shift = (1f - SCALE) / 2f;
        return world.spawn(new Location(world, at.x(), at.y(), at.z()), BlockDisplay.class, display -> {
            display.setPersistent(false);
            display.setVisibleByDefault(false);
            display.setBlock(block.getBlockData());
            display.setGlowing(true);
            display.setGlowColorOverride(Color.fromRGB(color));
            display.setBrightness(new Display.Brightness(15, 15));
            display.setTransformation(new Transformation(new Vector3f(shift, shift, shift), new AxisAngle4f(),
                    new Vector3f(SCALE, SCALE, SCALE), new AxisAngle4f()));
        });
    }

    /** Takes one device's outline down at once, when the device is removed. */
    void remove(Position at) {
        Outline outline = outlines.remove(at);
        if (outline != null) outline.display.remove();
    }

    /** True while an outline is up at this spot. */
    boolean showing(Position at) {
        return outlines.containsKey(at);
    }

    int size() { return outlines.size(); }

    void removeAll() {
        for (Outline outline : outlines.values()) outline.display.remove();
        outlines.clear();
    }
}
