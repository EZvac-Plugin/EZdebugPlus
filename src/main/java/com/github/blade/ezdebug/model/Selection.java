package com.github.blade.ezdebug.model;

import com.github.blade.hvac.api.ClimateUnitView;
import com.github.blade.hvac.api.EquipmentView;
import com.github.blade.hvac.api.HvacDevice;
import com.github.blade.hvac.api.InstrumentView;
import com.github.blade.hvac.api.ThermostatView;
import com.github.blade.hvac.api.VentView;

import java.util.Optional;

/**
 * Which devices belong to the system an EZvac link tool has selected, so
 * they can be drawn in the selected colour. Pure.
 */
public final class Selection {
    private Selection() {}

    /** The system a device is linked to, or empty for one in no system (a sensor, an unlinked sign). */
    public static Optional<String> systemOf(HvacDevice device) {
        return switch (device) {
            case ThermostatView thermostat -> Optional.of(thermostat.system());
            case EquipmentView equipment -> Optional.of(equipment.system());
            case VentView vent -> Optional.of(vent.system());
            case InstrumentView instrument -> instrument.system();
            case ClimateUnitView ignored -> Optional.empty();
        };
    }

    /** True when the device is linked to this system. */
    public static boolean in(HvacDevice device, String system) {
        return system != null && systemOf(device).map(system::equals).orElse(false);
    }
}
