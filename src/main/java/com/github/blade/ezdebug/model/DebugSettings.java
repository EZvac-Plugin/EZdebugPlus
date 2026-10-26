package com.github.blade.ezdebug.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Everything in config.yml, checked. A bad value warns and its part falls
 * back to the defaults; the rest load as written.
 *
 * @param durationSeconds how long a Debug Potion lasts
 * @param radius          devices are shown within this many blocks of the player
 * @param refreshTicks    how often outlines and clouds are redrawn
 * @param effect          the vanilla effect that shows the countdown (a key such as night_vision), or "none"
 * @param palette         the colours
 * @param linkTool        whether holding EZvac's link tool gives debug vision (EZvac 3.0, API 1.3)
 */
public record DebugSettings(int durationSeconds, double radius, long refreshTicks, String effect, Palette palette,
                            boolean linkTool) {
    public DebugSettings {
        if (durationSeconds < 1) throw new IllegalArgumentException("duration-minutes must be above 0");
        if (!(radius >= 4 && radius <= 128)) throw new IllegalArgumentException("radius must be 4 to 128 blocks");
        if (refreshTicks < 2 || refreshTicks > 100) throw new IllegalArgumentException("refresh-ticks must be 2 to 100");
        effect = effect == null || effect.isBlank() ? "none" : effect.trim().toLowerCase(Locale.ROOT);
    }

    public static DebugSettings defaults() {
        return load(new MemoryConfiguration(), warning -> { });
    }

    public long durationTicks() { return durationSeconds * 20L; }

    public static DebugSettings load(ConfigurationSection config, Consumer<String> warn) {
        Palette p = Palette.defaults();
        Palette palette = part("colors", warn, () -> new Palette(
                color(config, "colors.thermostat", p.thermostat()), color(config, "colors.thermometer", p.thermometer()),
                color(config, "colors.rpm-monitor", p.rpmMonitor()), color(config, "colors.settings-panel", p.settingsPanel()),
                color(config, "colors.sensor-hub", p.sensorHub()), color(config, "colors.climate-unit", p.climateUnit()),
                color(config, "colors.stopped", p.stopped()), color(config, "colors.cooling", p.cooling()),
                color(config, "colors.heating", p.heating()), color(config, "colors.selected", p.selected())), () -> p);
        boolean linkTool = config.getBoolean("link-tool", true);
        return part("debug", warn, () -> new DebugSettings(
                (int) Math.round(config.getDouble("duration-minutes", 8.0) * 60.0),
                config.getDouble("radius", 32.0), config.getLong("refresh-ticks", 10L),
                config.getString("effect", "night_vision"), palette, linkTool),
                () -> new DebugSettings(480, 32.0, 10L, "night_vision", palette, linkTool));
    }

    /** {@code "#RRGGBB"} as 0xRRGGBB. */
    static int color(ConfigurationSection config, String path, int fallback) {
        String text = config.getString(path);
        if (text == null) return fallback;
        String hex = text.trim();
        if (!hex.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException(path + " must be #RRGGBB, not '" + text + "'");
        return Integer.parseInt(hex.substring(1), 16);
    }

    private static <T> T part(String name, Consumer<String> warn, Supplier<T> reader, Supplier<T> fallback) {
        try {
            return reader.get();
        } catch (IllegalArgumentException invalid) {
            warn.accept(invalid.getMessage() + ", so the " + name + " settings use their defaults until config.yml is fixed.");
            return fallback.get();
        }
    }
}
