# EZdebug+
<img width="2560" height="2560" alt="EZDB" src="https://github.com/user-attachments/assets/fbb01ccb-a442-4cb4-afaf-bdb9b2eb9b3b" />

**See EZvac at work.** An addon for [EZvac](https://github.com/EZvac-Plugin/EZvac),
the realistic HVAC and indoor-climate plugin for Paper and Purpur.

`/ezdb debug` gives you a **Debug Potion**. Drink it, and for 8 minutes:
- **every EZvac sign gets a coloured frame:** a ring of specks around its
  board, in a colour for its kind;
- **every piece of equipment glows** with an outline you can see through
  walls, so equipment hidden in a casing is easy to find: grey while it's
  stopped, cyan while it cools, orange while it heats;
- **working vents puff a little cloud,** cyan or orange, while their system
  cools or heats, and so does running equipment.

Only you see any of it: other players see nothing. Night Vision shows the
countdown and helps in dark plant rooms. **Milk** ends it, like any potion,
and so does dying.

**While linking** (EZvac 3.0 or newer), you don't need the potion: hold
EZvac's link tool and debug vision is on for as long as it's in your hand.
The system the tool has selected stands out in red:
- its signs are framed in red;
- its equipment gets a red box;
- its vents get a red marker, even when idle.

So you see exactly what you're linking to, and what belongs to the house
next door.

> **Requires** EZvac ALPHA 2.2 or newer (EZvac API 1.2), on Paper or Purpur
> 1.21.1 with Java 21. Link-tool vision needs EZvac ALPHA 3.0 (API 1.3).

## Colours

| Device | Colour |
| --- | --- |
| Thermostats | aqua |
| Thermometers, hygrometers, thermo-hygrometers | white |
| RPM monitors | yellow |
| Settings panels | pink |
| Sensor hubs | green |
| Dehumidifier and humidifier signs | blue |
| Equipment | grey when stopped, cyan cooling, orange heating |
| The link tool's selected system | red frames, boxes and vent markers |

Clouds are cyan for cooling and orange for heating. Every colour can be
changed in config.yml. Signs get a frame rather than a glow: Minecraft draws
sign boards in a way a glowing copy can't trace.

## Commands and permissions

| Command | Does |
| --- | --- |
| `/ezdb debug [player]` | Gives a Debug Potion, to you or to someone else |
| `/ezdb off [player]` | Ends debug vision at once |
| `/ezdb reload` | Reloads config.yml |

All need `ezdebug.use` (ops by default). `/ezdebug` works too. Link-tool
vision needs `ezdebug.use` or EZvac's `ezvac.admin`.

## Configuration

`plugins/EZdebugPlus/config.yml`, explained line by line:
- `duration-minutes` (8);
- `radius`: how close devices must be (32 blocks);
- `refresh-ticks`: how often the view is redrawn (10, twice a second);
- `effect`: the countdown effect (`night_vision`, or `none`);
- `link-tool`: whether holding EZvac's link tool shows debug vision (`true`);
- `colors`, including `selected` for the link tool's system.

A bad value warns in the console and that part uses its defaults.

## How it works

EZdebug+ is built only on EZvac's public API: `devices(world)` lists every
device with its position, the equipment and thermostat views say what's
running, and the device events keep the outlines in step. `linkTool()` (API
1.3) says whether a player holds the link tool and which system it has
selected. Equipment outlines
are block displays that only debug players are shown; they're never saved
with the world. Frames and clouds are particles sent to debug players only.
See [EZvac's API guide](https://github.com/EZvac-Plugin/EZvac/blob/main/docs/api.md).

## Building

```shell
mvn clean package
```

The jar is written to `target/EZdebugPlus-<version>.jar`.

## License

MIT; see [LICENSE](LICENSE).
