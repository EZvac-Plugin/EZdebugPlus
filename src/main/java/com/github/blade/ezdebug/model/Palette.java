package com.github.blade.ezdebug.model;

import com.github.blade.hvac.api.ClimateUnitView;
import com.github.blade.hvac.api.EquipmentView;
import com.github.blade.hvac.api.HvacDevice;
import com.github.blade.hvac.api.InstrumentView;
import com.github.blade.hvac.api.ThermostatView;
import com.github.blade.hvac.api.VentView;

import java.util.OptionalInt;

/**
 * The colours debug vision draws in, as 0xRRGGBB. Every EZvac sign is
 * framed in its kind's colour; equipment is outlined by what it is doing;
 * clouds show cooling or heating. The system selected on a link tool is
 * drawn in {@code selected} for the player holding it. Pure.
 */
public record Palette(int thermostat, int thermometer, int rpmMonitor, int settingsPanel, int sensorHub,
                      int climateUnit, int stopped, int cooling, int heating, int selected) {
    public static Palette defaults() {
        return new Palette(0x55FFFF, 0xFFFFFF, 0xFFFF55, 0xFF77FF, 0x55FF55, 0x5599FF, 0x8A8A8A, 0x55DDFF, 0xFF9933,
                0xFF4444);
    }

    /**
     * A device's colour: its sign's frame, or equipment's outline; empty for
     * vents and sensor buttons. Air purifiers (EZvac 3.2) are climate units.
     * Any instrument kind this build doesn't know, such as 3.2's air monitor,
     * is a sign read like a thermometer, or a button when the API says it is
     * a sensor: a switch with no default throws on a kind added after it was
     * built.
     */
    public OptionalInt color(HvacDevice device) {
        return switch (device) {
            case ThermostatView ignored -> OptionalInt.of(thermostat);
            case EquipmentView equipment -> OptionalInt.of(equipment.running() ? activity(equipment.mode()) : stopped);
            case ClimateUnitView ignored -> OptionalInt.of(climateUnit);
            case VentView ignored -> OptionalInt.empty();
            case InstrumentView instrument -> instrument.kind().sensor() ? OptionalInt.empty()
                    : switch (instrument.kind()) {
                case RPM_MONITOR -> OptionalInt.of(rpmMonitor);
                case SETTINGS_PANEL -> OptionalInt.of(settingsPanel);
                case SENSOR_HUB -> OptionalInt.of(sensorHub);
                default -> OptionalInt.of(thermometer);
            };
        };
    }

    /** Cooling or heating, for clouds and running equipment; a unit running in IDLE reads as stopped. */
    public int activity(ThermostatView.Mode mode) {
        return switch (mode) {
            case COOLING -> cooling;
            case HEATING -> heating;
            case IDLE -> stopped;
        };
    }
}
