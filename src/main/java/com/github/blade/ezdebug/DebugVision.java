package com.github.blade.ezdebug;

import com.github.blade.ezdebug.model.Activity;
import com.github.blade.ezdebug.model.DebugSettings;
import com.github.blade.ezdebug.model.Palette;
import com.github.blade.hvac.api.EquipmentView;
import com.github.blade.hvac.api.EzvacApi;
import com.github.blade.hvac.api.HvacDevice;
import com.github.blade.hvac.api.Position;
import com.github.blade.hvac.api.ThermostatView;
import com.github.blade.hvac.api.VentView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Who has debug vision, until when, and what they see: outlines on every
 * EZvac sign and piece of equipment near them, and clouds on vents and
 * equipment at work. Main thread only.
 */
final class DebugVision {
    private final EzDebugPlugin plugin;
    private final Outlines outlines;
    /** Each debug player's end, in server ticks. */
    private final Map<UUID, Long> endsAt = new HashMap<>();
    private BukkitTask task;
    private long tick;

    DebugVision(EzDebugPlugin plugin) {
        this.plugin = plugin;
        this.outlines = new Outlines(plugin);
    }

    void start() {
        stopTask();
        long period = plugin.settings().refreshTicks();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            tick += period;
            refresh();
        }, period, period);
    }

    private void stopTask() {
        if (task != null) task.cancel();
        task = null;
    }

    void shutdown() {
        stopTask();
        outlines.removeAll();
    }

    boolean active(Player player) {
        return endsAt.containsKey(player.getUniqueId());
    }

    /** Ticks of debug vision a player has left, 0 when none. */
    long remaining(Player player) {
        Long end = endsAt.get(player.getUniqueId());
        return end == null ? 0L : Math.max(0L, end - tick);
    }

    /** Debug vision on for this long, from now (a new potion starts the clock again). */
    void begin(Player player, long ticks) {
        endsAt.put(player.getUniqueId(), tick + ticks);
        refresh();
    }

    /** Debug vision off. Quietly, when the player is leaving. */
    void end(Player player, boolean tell) {
        if (endsAt.remove(player.getUniqueId()) == null) return;
        refresh();
        if (tell) player.sendMessage(Component.text("Debug vision fades.", NamedTextColor.GRAY));
    }

    /** A device was removed: its outline goes at once. */
    void forget(Position at) {
        outlines.remove(at);
    }

    boolean outlined(Position at) {
        return outlines.showing(at);
    }

    int outlineCount() {
        return outlines.size();
    }

    /** Redraws everything: called on the task, and when someone starts or stops. */
    void refresh() {
        List<Player> viewers = new ArrayList<>();
        for (UUID id : new ArrayList<>(endsAt.keySet())) {
            Player player = plugin.getServer().getPlayer(id);
            if (player == null) continue;
            if (endsAt.get(id) <= tick) {
                endsAt.remove(id);
                player.sendMessage(Component.text("Debug vision fades.", NamedTextColor.GRAY));
                continue;
            }
            viewers.add(player);
        }
        Map<Position, Outlines.Wanted> wanted = new HashMap<>();
        Map<World, List<Player>> byWorld = new HashMap<>();
        for (Player player : viewers) byWorld.computeIfAbsent(player.getWorld(), ignored -> new ArrayList<>()).add(player);
        for (Map.Entry<World, List<Player>> entry : byWorld.entrySet()) draw(entry.getKey(), entry.getValue(), wanted);
        outlines.show(wanted);
    }

    private void draw(World world, List<Player> players, Map<Position, Outlines.Wanted> wanted) {
        EzvacApi ezvac = plugin.ezvac();
        DebugSettings settings = plugin.settings();
        Palette palette = settings.palette();
        double radius = settings.radius();
        Activity activity = new Activity(ezvac.thermostats(world));
        for (HvacDevice device : ezvac.devices(world)) {
            List<Player> near = new ArrayList<>();
            for (Player player : players) if (near(player, device.position(), radius)) near.add(player);
            if (near.isEmpty()) continue;
            OptionalInt color = palette.outline(device);
            if (color.isPresent()) wanted.put(device.position(), new Outlines.Wanted(color.getAsInt(), new HashSet<>(near)));
            Optional<ThermostatView.Mode> working = switch (device) {
                case VentView vent -> activity.vent(vent);
                case EquipmentView equipment -> Activity.equipment(equipment);
                default -> Optional.empty();
            };
            if (working.isPresent()) for (Player player : near) cloud(player, device.position(), palette.activity(working.get()));
        }
    }

    private static boolean near(Player player, Position at, double radius) {
        double dx = player.getLocation().getX() - (at.x() + 0.5);
        double dy = player.getLocation().getY() - (at.y() + 0.5);
        double dz = player.getLocation().getZ() - (at.z() + 0.5);
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    /** A little coloured cloud on a block, for this player's eyes only. */
    private static void cloud(Player player, Position at, int rgb) {
        player.spawnParticle(Particle.DUST, at.x() + 0.5, at.y() + 0.6, at.z() + 0.5, 6, 0.25, 0.2, 0.25, 0.0,
                new Particle.DustOptions(Color.fromRGB(rgb), 1.2f));
    }
}
