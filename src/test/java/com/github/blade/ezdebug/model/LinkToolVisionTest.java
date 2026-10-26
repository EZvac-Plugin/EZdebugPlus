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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** What the link tool's selection marks, and the box drawn around selected equipment. */
class LinkToolVisionTest {
    private static final UUID WORLD = UUID.fromString("0b6c3f0e-6a77-4f3e-9d6b-2f1c9a7e4d10");
    private static final Position AT = new Position(WORLD, 1, 64, 1);

    @Test
    void devicesAreMarkedWhenTheyAreInTheSelectedSystem() {
        ThermostatView home = new ThermostatView("Home", "#7", AT, 70, 72, ThermostatView.Mode.IDLE, true, false, 40, 0, 1);
        EquipmentView pump = new EquipmentView(EquipmentStateChangeEvent.Kind.HEAT_PUMP, "#7", AT, true, false,
                ThermostatView.Mode.IDLE, 0);
        VentView vent = new VentView("#7", AT, VentView.Medium.AIR);
        InstrumentView monitor = new InstrumentView(InstrumentView.Kind.RPM_MONITOR, "Fans", AT, Optional.of("#7"), List.of());
        for (var device : List.of(home, pump, vent, monitor)) {
            assertTrue(Selection.in(device, "#7"), device.toString());
            assertFalse(Selection.in(device, "house"), "another system");
            assertFalse(Selection.in(device, null), "nothing selected");
        }
        InstrumentView loose = new InstrumentView(InstrumentView.Kind.SETTINGS_PANEL, "Loose", AT, Optional.empty(), List.of());
        assertEquals(Optional.empty(), Selection.systemOf(loose), "an unlinked sign is in no system");
        assertEquals(Optional.empty(), Selection.systemOf(new ClimateUnitView("dry", ClimateUnitView.Kind.DEHUMIDIFIER,
                AT, 50, 40, true, ClimateUnitView.State.IDLE, 0)), "dehumidifiers need no system");
    }

    @Test
    void theBoxRunsAlongEveryEdgeJustOutsideTheBlock() {
        List<BoxFrame.Point> points = BoxFrame.points();
        assertEquals(12 * BoxFrame.PER_EDGE - 16, points.size(), "each of the 8 corners counted once");
        assertEquals(points.size(), new HashSet<>(points).size(), "no speck drawn twice");
        double low = -BoxFrame.OUTSET;
        double high = 1 + BoxFrame.OUTSET;
        for (BoxFrame.Point point : points) {
            int onFaces = 0;
            for (double value : new double[]{point.x(), point.y(), point.z()})
                if (Math.abs(value - low) < 1e-9 || Math.abs(value - high) < 1e-9) onFaces++;
            assertTrue(onFaces >= 2, "on an edge: " + point);
        }
    }

    @Test
    void theSelectedColourAndTheSwitchAreInTheConfig() {
        assertEquals(0xFF4444, Palette.defaults().selected(), "unlike every kind's colour");
        assertTrue(DebugSettings.defaults().linkTool());
        YamlConfiguration config = new YamlConfiguration();
        config.set("link-tool", false);
        config.set("colors.selected", "#00FF00");
        DebugSettings loaded = DebugSettings.load(config, warning -> fail(warning));
        assertFalse(loaded.linkTool());
        assertEquals(0x00FF00, loaded.palette().selected());
    }
}
