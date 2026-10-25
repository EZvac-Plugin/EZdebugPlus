package com.github.blade.ezdebug.model;

import com.github.blade.hvac.api.EquipmentView;
import com.github.blade.hvac.api.ThermostatView;
import com.github.blade.hvac.api.VentView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Which vents and equipment are at work right now, from EZvac's views of one world. Pure. */
public final class Activity {
    private final Map<String, ThermostatView.Mode> working = new HashMap<>();

    /** @param thermostats the thermostats of one world: system names are unique within a world */
    public Activity(Collection<ThermostatView> thermostats) {
        for (ThermostatView thermostat : thermostats)
            if (thermostat.enabled() && thermostat.mode() != ThermostatView.Mode.IDLE)
                working.put(thermostat.system(), thermostat.mode());
    }

    /** What a vent is blowing: its system's mode while the system cools or heats, else empty. */
    public Optional<ThermostatView.Mode> vent(VentView vent) {
        return Optional.ofNullable(working.get(vent.system()));
    }

    /** What a unit is doing while it runs, else empty. */
    public static Optional<ThermostatView.Mode> equipment(EquipmentView equipment) {
        if (!equipment.running() || equipment.mode() == ThermostatView.Mode.IDLE) return Optional.empty();
        return Optional.of(equipment.mode());
    }
}
