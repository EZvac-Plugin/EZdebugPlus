package com.github.blade.ezdebug;

import com.github.blade.ezdebug.model.Activity;
import com.github.blade.ezdebug.model.DebugSettings;
import com.github.blade.ezdebug.model.BoxFrame;
import com.github.blade.ezdebug.model.Dots;
import com.github.blade.ezdebug.model.Palette;
import com.github.blade.ezdebug.model.Selection;
import com.github.blade.ezdebug.model.SignFrame;
import com.github.blade.hvac.api.EquipmentView;
import com.github.blade.hvac.api.EzvacApi;
import com.github.blade.hvac.api.EzvacLinkTool;
import com.github.blade.hvac.api.HvacDevice;
import com.github.blade.hvac.api.Position;
import com.github.blade.hvac.api.ThermostatView;
import com.github.blade.hvac.api.VentView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.WallHangingSign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
 * Who has debug vision, until when, and what they see: a coloured frame
 * around every EZvac sign near them, glowing outlines on equipment, and
 * clouds on vents and equipment at work. A Debug Potion gives it for a
 * while; holding EZvac's link tool gives it for as long as it is in hand,
 * with the system the tool has selected drawn in its own colour. Main
 * thread only.
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
        // The system each link-tool holder has selected; null when it has none (or a sensor hub).
        Map<UUID, String> selecting = new HashMap<>();
        if (plugin.toolVision()) {
            EzvacLinkTool tool = plugin.ezvac().linkTool();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.hasPermission("ezdebug.use") && !player.hasPermission("ezvac.admin")) continue;
                ItemStack held = player.getInventory().getItemInMainHand();
                if (!tool.isLinkTool(held)) continue;
                selecting.put(player.getUniqueId(),
                        tool.selection(held).flatMap(EzvacLinkTool.LinkSelection::system).orElse(null));
                if (!viewers.contains(player)) viewers.add(player);
            }
        }
        Map<Position, Outlines.Wanted> wanted = new HashMap<>();
        Map<World, List<Player>> byWorld = new HashMap<>();
        for (Player player : viewers) byWorld.computeIfAbsent(player.getWorld(), ignored -> new ArrayList<>()).add(player);
        for (Map.Entry<World, List<Player>> entry : byWorld.entrySet()) {
            draw(entry.getKey(), entry.getValue(), selecting, wanted);
            stations(entry.getKey(), entry.getValue());
        }
        outlines.show(wanted);
    }

    /** A weather station's area, drawn in this colour. */
    private static final int STATION = 0x7FB2FF;

    /**
     * Rooms as EZvac sees them (EZvac API 1.10): the systems each viewer
     * sees, every one near a potion drinker or only the one a link-tool
     * holder has selected, have their edges dotted in and their leaks
     * outlined in glowing red.
     */
    private void rooms(World world, Map<Player, java.util.Collection<String>> seen,
                       Map<Position, Outlines.Wanted> wanted) {
        if (seen.isEmpty() || !Spaces.offered(plugin.ezvac().apiVersion())) return;
        Map<Position, List<Player>> leaks = Spaces.draw(plugin.ezvac(), world, seen, tick);
        int color = plugin.settings().palette().selected();
        // A leak is shown as glowing red glass filling the opening.
        org.bukkit.block.data.BlockData glass = org.bukkit.Material.RED_STAINED_GLASS.createBlockData();
        for (Map.Entry<Position, List<Player>> leak : leaks.entrySet())
            wanted.putIfAbsent(leak.getKey(), new Outlines.Wanted(color, new HashSet<>(leak.getValue()), glass, 0.96f));
    }

    /** Each weather station's area (EZvac API 1.7), a dotted square at its height, near the viewer only. */
    private void stations(World world, List<Player> players) {
        if (!atLeast(plugin.ezvac().apiVersion(), 7)) return;
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(STATION), 1.0f);
        for (var station : plugin.ezvac().weatherStations(world)) {
            Position at = station.position();
            for (Player player : players) {
                double radius = plugin.settings().radius();
                double dx = player.getLocation().getX() - (at.x() + 0.5), dz = player.getLocation().getZ() - (at.z() + 0.5);
                double reach = station.areaRadius() + radius;
                if (dx * dx + dz * dz > reach * reach) continue;
                for (Dots.Point point : Dots.square(at.x() + 0.5, at.z() + 0.5, at.y() + 0.5, station.areaRadius(),
                        player.getLocation().getX(), player.getLocation().getZ(), radius))
                    player.spawnParticle(Particle.DUST, point.x(), point.y(), point.z(), 1, 0.0, 0.0, 0.0, 0.0, dust);
            }
        }
    }

    /** A dotted line between two devices, in a colour, for these players. */
    private static void link(Position from, Position to, int rgb, List<Player> players) {
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(rgb), 0.5f);
        for (Dots.Point point : Dots.line(from.x() + 0.5, from.y() + 0.5, from.z() + 0.5, to.x() + 0.5, to.y() + 0.5,
                to.z() + 0.5))
            for (Player player : players)
                player.spawnParticle(Particle.DUST, point.x(), point.y(), point.z(), 1, 0.0, 0.0, 0.0, 0.0, dust);
    }

    /** API 1.{minor} or newer, within major version 1. */
    static boolean atLeast(String apiVersion, int minor) {
        String[] parts = apiVersion.split("\\.");
        try {
            return parts.length >= 2 && Integer.parseInt(parts[0]) == 1 && Integer.parseInt(parts[1]) >= minor;
        } catch (NumberFormatException unknown) {
            return false;
        }
    }

    private void draw(World world, List<Player> players, Map<UUID, String> selecting,
                      Map<Position, Outlines.Wanted> wanted) {
        EzvacApi ezvac = plugin.ezvac();
        DebugSettings settings = plugin.settings();
        Palette palette = settings.palette();
        double radius = settings.radius();
        Activity activity = new Activity(ezvac.thermostats(world));
        // The systems each viewer sees the room of: every one near a potion drinker, the selection while linking.
        Map<Player, java.util.Collection<String>> rooms = new HashMap<>();
        for (Player player : players) {
            String chosen = selecting.get(player.getUniqueId());
            if (active(player)) rooms.put(player, new HashSet<>());
            else if (chosen != null) rooms.put(player, new HashSet<>(List.of(chosen)));
        }
        boolean links = atLeast(ezvac.apiVersion(), 3);
        for (HvacDevice device : ezvac.devices(world)) {
            List<Player> near = new ArrayList<>();
            for (Player player : players) if (near(player, device.position(), radius)) near.add(player);
            if (near.isEmpty()) continue;
            Optional<String> system = Selection.systemOf(device);
            if (system.isPresent())
                for (Player player : near) if (active(player)) rooms.get(player).add(system.get());
            // The links nothing else shows: a hub to its sensors, an RPM monitor to its equipment, a panel to its thermostat.
            if (links && device instanceof com.github.blade.hvac.api.InstrumentView instrument) {
                OptionalInt tint = palette.color(device);
                int rgb = tint.isPresent() ? tint.getAsInt() : palette.sensorHub();
                for (Position linked : instrument.links()) link(device.position(), linked, rgb, near);
                if (instrument.kind() == com.github.blade.hvac.api.InstrumentView.Kind.SETTINGS_PANEL && instrument.system().isPresent())
                    ezvac.thermostatOf(world, instrument.system().get())
                            .ifPresent(thermostat -> link(device.position(), thermostat.position(), rgb, near));
            }
            OptionalInt color = palette.color(device);
            // The players whose link tool has this device's system selected see it in the selected colour.
            List<Player> linking = new ArrayList<>();
            List<Player> others = new ArrayList<>();
            for (Player player : near)
                (Selection.in(device, selecting.get(player.getUniqueId())) ? linking : others).add(player);
            if (color.isPresent()) {
                if (device instanceof EquipmentView) {
                    // One outline is shared by every viewer, so the selection is a box of specks on top.
                    wanted.put(device.position(), new Outlines.Wanted(color.getAsInt(), new HashSet<>(near)));
                    if (!linking.isEmpty()) box(device.position(), palette.selected(), linking);
                } else {
                    if (!others.isEmpty()) frame(world, device.position(), color.getAsInt(), others);
                    if (!linking.isEmpty()) frame(world, device.position(), palette.selected(), linking);
                }
            } else if (device instanceof VentView) {
                // Even an idle vent of the selected system is marked, so every linked vent can be found.
                for (Player player : linking) marker(player, device.position(), palette.selected());
            }
            Optional<ThermostatView.Mode> working = switch (device) {
                case VentView vent -> activity.vent(vent);
                case EquipmentView equipment -> Activity.equipment(equipment);
                default -> Optional.empty();
            };
            if (working.isPresent()) for (Player player : near) cloud(player, device.position(), palette.activity(working.get()));
        }
        rooms(world, rooms, wanted);
    }

    private static boolean near(Player player, Position at, double radius) {
        double dx = player.getLocation().getX() - (at.x() + 0.5);
        double dy = player.getLocation().getY() - (at.y() + 0.5);
        double dz = player.getLocation().getZ() - (at.z() + 0.5);
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    /**
     * A dotted frame around a sign's board, in its kind's colour, for these
     * players' eyes only; a device on something else (a mini thermostat's
     * wall button) gets a box round its block.
     */
    private static void frame(World world, Position at, int rgb, List<Player> players) {
        if (!world.isChunkLoaded(at.x() >> 4, at.z() >> 4)) return;
        Block block = world.getBlockAt(at.x(), at.y(), at.z());
        if (!Tag.ALL_SIGNS.isTagged(block.getType())) {
            if (!block.getType().isAir()) box(at, rgb, players);
            return;
        }
        BlockData sign = block.getBlockData();
        SignFrame.Mount mount = sign instanceof WallSign ? SignFrame.Mount.WALL
                : sign instanceof WallHangingSign || sign instanceof HangingSign ? SignFrame.Mount.HANGING
                : SignFrame.Mount.STANDING;
        BlockFace facing = sign instanceof Directional directional ? directional.getFacing()
                : sign instanceof Rotatable rotatable ? rotatable.getRotation() : BlockFace.SOUTH;
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(rgb), 0.6f);
        for (SignFrame.Point point : SignFrame.of(mount, facing.getDirection().getX(), facing.getDirection().getZ()))
            for (Player player : players)
                player.spawnParticle(Particle.DUST, at.x() + point.x(), at.y() + point.y(), at.z() + point.z(), 1,
                        0.0, 0.0, 0.0, 0.0, dust);
    }

    /** A dotted box around a block, for these players' eyes only. */
    private static void box(Position at, int rgb, List<Player> players) {
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(rgb), 0.7f);
        for (BoxFrame.Point point : BoxFrame.points())
            for (Player player : players)
                player.spawnParticle(Particle.DUST, at.x() + point.x(), at.y() + point.y(), at.z() + point.z(), 1,
                        0.0, 0.0, 0.0, 0.0, dust);
    }

    /** One bright speck just above a block, for this player's eyes only. */
    private static void marker(Player player, Position at, int rgb) {
        player.spawnParticle(Particle.DUST, at.x() + 0.5, at.y() + 1.05, at.z() + 0.5, 2, 0.08, 0.0, 0.08, 0.0,
                new Particle.DustOptions(Color.fromRGB(rgb), 1.4f));
    }

    /**
     * A coloured cloud on a block and a short column rising out of it, easy
     * to see (0.4.0), for this player's eyes only.
     */
    private static void cloud(Player player, Position at, int rgb) {
        player.spawnParticle(Particle.DUST, at.x() + 0.5, at.y() + 0.7, at.z() + 0.5, 10, 0.3, 0.25, 0.3, 0.0,
                new Particle.DustOptions(Color.fromRGB(rgb), 1.8f));
        Particle.DustOptions column = new Particle.DustOptions(Color.fromRGB(rgb), 1.2f);
        for (int i = 1; i <= 4; i++)
            player.spawnParticle(Particle.DUST, at.x() + 0.5, at.y() + 0.8 + i * 0.35, at.z() + 0.5, 2, 0.08, 0.05,
                    0.08, 0.0, column);
    }
}
