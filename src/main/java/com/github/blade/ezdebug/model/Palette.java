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
 * clouds show cooling or heating. Pure.
 */
public record Palette(int thermostat, int thermometer, int rpmMonitor, int settingsPanel, int sensorHub,
                      int climateUnit, int stopped, int cooling, int heating) {
    public static Palette defaults() {
        return new Palette(0x55FFFF, 0xFFFFFF, 0xFFFF55, 0xFF77FF, 0x55FF55, 0x5599FF, 0x8A8A8A, 0x55DDFF, 0xFF9933);
    }

    /** A device's colour: its sign's frame, or equipment's outline; empty for vents and sensor buttons. */
    public OptionalInt color(HvacDevice device) {
        return switch (device) {
            case ThermostatView ignored -> OptionalInt.of(thermostat);
            case EquipmentView equipment -> OptionalInt.of(equipment.running() ? activity(equipment.mode()) : stopped);
            case ClimateUnitView ignored -> OptionalInt.of(climateUnit);
            case VentView ignored -> OptionalInt.empty();
            case InstrumentView instrument -> switch (instrument.kind()) {
                case THERMOMETER, HYGROMETER, THERMO_HYGROMETER -> OptionalInt.of(thermometer);
                case RPM_MONITOR -> OptionalInt.of(rpmMonitor);
                case SETTINGS_PANEL -> OptionalInt.of(settingsPanel);
                case SENSOR_HUB -> OptionalInt.of(sensorHub);
                case TEMPERATURE_SENSOR, HUMIDITY_SENSOR, TEMPERATURE_HUMIDITY_SENSOR, WEATHER_SENSOR -> OptionalInt.empty();
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
