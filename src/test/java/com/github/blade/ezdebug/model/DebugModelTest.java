package com.github.blade.ezdebug.model;

import com.github.blade.hvac.api.ClimateUnitView;
import com.github.blade.hvac.api.EquipmentView;
import com.github.blade.hvac.api.InstrumentView;
import com.github.blade.hvac.api.Position;
import com.github.blade.hvac.api.ThermostatView;
import com.github.blade.hvac.api.VentView;
import com.github.blade.hvac.api.event.EquipmentStateChangeEvent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The colours, what counts as at work, and config.yml. */
class DebugModelTest {
    private static final UUID WORLD = UUID.fromString("0b6c3f0e-6a77-4f3e-9d6b-2f1c9a7e4d10");
    private static final Position AT = new Position(WORLD, 1, 64, 1);
    private final Palette palette = Palette.defaults();

    private static ThermostatView thermostat(String system, ThermostatView.Mode mode, boolean enabled) {
        return new ThermostatView(system + "-t", system, AT, 70, 72, mode, enabled, false, 40, 1, 1);
    }

    private static EquipmentView equipment(boolean running, ThermostatView.Mode mode) {
        return new EquipmentView(EquipmentStateChangeEvent.Kind.FURNACE, "house", AT, true, running, mode,
                running ? 2700 : 0);
    }

    private static InstrumentView instrument(InstrumentView.Kind kind) {
        return new InstrumentView(kind, "x", AT, Optional.empty(), List.of());
    }

    @Test
    void everySignHasItsKindsColourAndButtonsAndVentsHaveNone() {
        assertEquals(OptionalInt.of(palette.thermostat()), palette.color(thermostat("house", ThermostatView.Mode.IDLE, true)));
        assertEquals(OptionalInt.of(palette.thermometer()), palette.color(instrument(InstrumentView.Kind.HYGROMETER)));
        assertEquals(OptionalInt.of(palette.rpmMonitor()), palette.color(instrument(InstrumentView.Kind.RPM_MONITOR)));
        assertEquals(OptionalInt.of(palette.settingsPanel()), palette.color(instrument(InstrumentView.Kind.SETTINGS_PANEL)));
        assertEquals(OptionalInt.of(palette.sensorHub()), palette.color(instrument(InstrumentView.Kind.SENSOR_HUB)));
        assertEquals(OptionalInt.of(palette.climateUnit()), palette.color(new ClimateUnitView("dry",
                ClimateUnitView.Kind.DEHUMIDIFIER, AT, 50, 40, true, ClimateUnitView.State.IDLE, 0)));
        for (InstrumentView.Kind kind : InstrumentView.Kind.values())
            if (kind.sensor()) assertEquals(OptionalInt.empty(), palette.color(instrument(kind)), kind + " is a button");
        assertEquals(OptionalInt.empty(), palette.color(new VentView("house", AT, VentView.Medium.AIR)));
    }

    @Test
    void equipmentIsColouredByWhatItDoes() {
        assertEquals(OptionalInt.of(palette.stopped()), palette.color(equipment(false, ThermostatView.Mode.IDLE)));
        assertEquals(OptionalInt.of(palette.heating()), palette.color(equipment(true, ThermostatView.Mode.HEATING)));
        assertEquals(OptionalInt.of(palette.cooling()), palette.color(equipment(true, ThermostatView.Mode.COOLING)));
        assertEquals(OptionalInt.of(palette.stopped()), palette.color(equipment(true, ThermostatView.Mode.IDLE)));
    }

    @Test
    void ventsWorkWhileTheirSystemCoolsOrHeats() {
        Activity activity = new Activity(List.of(thermostat("house", ThermostatView.Mode.HEATING, true),
                thermostat("office", ThermostatView.Mode.IDLE, true),
                thermostat("garage", ThermostatView.Mode.COOLING, false)));
        assertEquals(Optional.of(ThermostatView.Mode.HEATING), activity.vent(new VentView("house", AT, VentView.Medium.AIR)));
        assertEquals(Optional.empty(), activity.vent(new VentView("office", AT, VentView.Medium.AIR)), "idle");
        assertEquals(Optional.empty(), activity.vent(new VentView("garage", AT, VentView.Medium.AIR)), "switched off");
        assertEquals(Optional.empty(), activity.vent(new VentView("shed", AT, VentView.Medium.AIR)), "no thermostat");
        assertEquals(Optional.of(ThermostatView.Mode.HEATING), Activity.equipment(equipment(true, ThermostatView.Mode.HEATING)));
        assertEquals(Optional.empty(), Activity.equipment(equipment(false, ThermostatView.Mode.IDLE)));
    }

    @Test
    void theBundledConfigIsTheDefaults() throws Exception {
        YamlConfiguration bundled = new YamlConfiguration();
        try (var reader = new InputStreamReader(Objects.requireNonNull(
                DebugModelTest.class.getResourceAsStream("/config.yml")), StandardCharsets.UTF_8)) {
            bundled.load(reader);
        }
        List<String> warnings = new ArrayList<>();
        assertEquals(DebugSettings.defaults(), DebugSettings.load(bundled, warnings::add));
        assertTrue(warnings.isEmpty(), warnings::toString);
        DebugSettings defaults = DebugSettings.defaults();
        assertEquals(480, defaults.durationSeconds(), "8 minutes");
        assertEquals(9600, defaults.durationTicks());
        assertEquals("night_vision", defaults.effect());
    }

    @Test
    void badValuesWarnAndFallBack() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("colors.heating", "orange");
        config.set("radius", 2000);
        config.set("duration-minutes", 3);
        List<String> warnings = new ArrayList<>();
        DebugSettings loaded = DebugSettings.load(config, warnings::add);
        assertEquals(2, warnings.size(), warnings::toString);
        assertEquals(Palette.defaults(), loaded.palette());
        assertEquals(32.0, loaded.radius());
        assertEquals(480, loaded.durationSeconds(), "the whole part falls back, as in LiveExposure");

        YamlConfiguration short_ = new YamlConfiguration();
        short_.set("duration-minutes", 0.5);
        short_.set("effect", "NONE");
        DebugSettings quick = DebugSettings.load(short_, warning -> fail(warning));
        assertEquals(30, quick.durationSeconds());
        assertEquals("none", quick.effect());
    }

    @Test
    void aSignsFrameRingsItsBoard() {
        // A wall sign facing north lies against the block to its south.
        List<SignFrame.Point> frame = SignFrame.of(SignFrame.Mount.WALL, 0, -1);
        assertEquals(2 * SignFrame.PER_SIDE + 2 * (SignFrame.PER_SIDE - 2), frame.size(), "corners counted once");
        double z = 0.5 - (-0.375 + SignFrame.AHEAD);
        for (SignFrame.Point point : frame) assertEquals(z, point.z(), 1e-9, "flat, just in front of the board");
        double minX = frame.stream().mapToDouble(SignFrame.Point::x).min().orElseThrow();
        double maxX = frame.stream().mapToDouble(SignFrame.Point::x).max().orElseThrow();
        assertEquals(1.1, maxX - minX, 1e-9, "a little wider than the board");
        double minY = frame.stream().mapToDouble(SignFrame.Point::y).min().orElseThrow();
        double maxY = frame.stream().mapToDouble(SignFrame.Point::y).max().orElseThrow();
        assertEquals(0.53, (minY + maxY) / 2, 1e-9, "centred on the board");
        // Facing east, the frame lies along z instead.
        List<SignFrame.Point> east = SignFrame.of(SignFrame.Mount.WALL, 1, 0);
        for (SignFrame.Point point : east) assertEquals(0.5 + (-0.375 + SignFrame.AHEAD), point.x(), 1e-9);
    }
}
