<div align="center">

<img width="220" alt="EZdebug+" src="https://github.com/user-attachments/assets/fbb01ccb-a442-4cb4-afaf-bdb9b2eb9b3b" />

# EZdebug+

**See EZvac at work.**<br>
An official addon for [EZvac](https://github.com/EZvac-Plugin/EZvac), real heating, cooling and indoor climate for Paper and Purpur.

[![Release](https://img.shields.io/github/v/release/EZvac-Plugin/EZdebugPlus?label=release&color=0e7c86&logo=github&style=for-the-badge&labelColor=1f2328)](https://github.com/EZvac-Plugin/EZdebugPlus/releases)
[![Needs EZvac](https://img.shields.io/badge/needs%20EZvac-ALPHA%202.2%2B-0e7c86?style=for-the-badge&labelColor=1f2328&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNiAxNiI+PHBhdGggZmlsbD0iI2ZmZiIgZmlsbC1ydWxlPSJldmVub2RkIiBkPSJNOCAxYzIuNCAyLjggNC42IDUuNSA0LjYgOC4yYTQuNiA0LjYgMCAwIDEtOS4yIDBDMy40IDYuNSA1LjYgMy44IDggMXptMCA2LjZhMS43IDEuNyAwIDEgMCAwIDMuNCAxLjcgMS43IDAgMCAwIDAtMy40eiIvPjwvc3ZnPg==)](https://github.com/EZvac-Plugin/EZvac)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1%2B-0e7c86?style=for-the-badge&labelColor=1f2328&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNiAxNiIgc2hhcGUtcmVuZGVyaW5nPSJjcmlzcEVkZ2VzIj48cmVjdCB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIGZpbGw9IiM4YTVhM2IiLz48cmVjdCB3aWR0aD0iMTYiIGhlaWdodD0iNSIgZmlsbD0iIzZhYWEzYSIvPjxyZWN0IHg9IjIiIHk9IjUiIHdpZHRoPSIyIiBoZWlnaHQ9IjIiIGZpbGw9IiM2YWFhM2EiLz48cmVjdCB4PSI3IiB5PSI1IiB3aWR0aD0iMiIgaGVpZ2h0PSIzIiBmaWxsPSIjNmFhYTNhIi8+PHJlY3QgeD0iMTIiIHk9IjUiIHdpZHRoPSIyIiBoZWlnaHQ9IjIiIGZpbGw9IiM2YWFhM2EiLz48cmVjdCB4PSI0IiB5PSIxMCIgd2lkdGg9IjIiIGhlaWdodD0iMiIgZmlsbD0iIzZiNDMyOSIvPjxyZWN0IHg9IjEwIiB5PSIxMiIgd2lkdGg9IjIiIGhlaWdodD0iMiIgZmlsbD0iIzZiNDMyOSIvPjxyZWN0IHg9IjEyIiB5PSI4IiB3aWR0aD0iMSIgaGVpZ2h0PSIxIiBmaWxsPSIjYTc3NzRmIi8+PHJlY3QgeD0iMiIgeT0iMTMiIHdpZHRoPSIxIiBoZWlnaHQ9IjEiIGZpbGw9IiNhNzc3NGYiLz48L3N2Zz4=)](#in-detail)
[![License](https://img.shields.io/github/license/EZvac-Plugin/EZdebugPlus?color=0e7c86&logo=opensourceinitiative&style=for-the-badge&labelColor=1f2328)](LICENSE)

[![Download](https://img.shields.io/badge/Download-f69500?style=for-the-badge&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNiAxNiI+PHBhdGggZmlsbD0iI2ZmZiIgZD0iTTcgMWgydjdsMi41LTIuNSAxLjQgMS40TDggMTEuOCAzLjEgNi45bDEuNC0xLjRMNyA4eiIvPjxwYXRoIGZpbGw9IiNmZmYiIGQ9Ik0yIDEyaDJ2MS41aDhWMTJoMnYzLjVIMnoiLz48L3N2Zz4=)](https://github.com/EZvac-Plugin/EZdebugPlus/releases/latest)
[![EZvac](https://img.shields.io/badge/EZvac-f69500?style=for-the-badge&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNiAxNiI+PHBhdGggZmlsbD0iI2ZmZiIgZmlsbC1ydWxlPSJldmVub2RkIiBkPSJNOCAxYzIuNCAyLjggNC42IDUuNSA0LjYgOC4yYTQuNiA0LjYgMCAwIDEtOS4yIDBDMy40IDYuNSA1LjYgMy44IDggMXptMCA2LjZhMS43IDEuNyAwIDEgMCAwIDMuNCAxLjcgMS43IDAgMCAwIDAtMy40eiIvPjwvc3ZnPg==)](https://github.com/EZvac-Plugin/EZvac)
[![Website](https://img.shields.io/badge/EZvac%20website-f69500?style=for-the-badge&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNiAxNiI+PHBhdGggZmlsbD0iI2ZmZiIgZmlsbC1ydWxlPSJldmVub2RkIiBkPSJNOCAxYTcgNyAwIDEgMCAwIDE0QTcgNyAwIDAgMCA4IDF6bS0uOCAxLjZDNi40IDMuNyA1LjkgNS4zIDUuOCA3LjJIMy4xYTUgNSAwIDAgMSA0LjEtNC42em0xLjYgMGE1IDUgMCAwIDEgNC4xIDQuNmgtMi43Yy0uMS0xLjktLjYtMy41LTEuNC00LjZ6TTcuMiA3LjJINy4zYy4xLTEuNi41LTMgLjctMy42LjIuNi42IDIgLjcgMy42ek0zLjEgOC44aDIuN2MuMSAxLjkuNiAzLjUgMS40IDQuNmE1IDUgMCAwIDEtNC4xLTQuNnptNC4yIDBoMS40Yy0uMSAxLjYtLjUgMy0uNyAzLjYtLjItLjYtLjYtMi0uNy0zLjZ6bTMuNiAwaDIuN2E1IDUgMCAwIDEtNC4xIDQuNmMuOC0xLjEgMS4zLTIuNyAxLjQtNC42eiIvPjwvc3ZnPg==)](https://ezvac-plugin.github.io/EZvac/)

</div>

## Features

<table>
<tr>
<td width="50%" valign="top">

**🧪 A Debug Potion**<br>
Drink it and, for 8 minutes, every EZvac system around you shows itself. Only you see it.

</td>
<td width="50%" valign="top">

**🔆 Equipment that glows**<br>
Through walls: grey while stopped, cyan while cooling, orange while heating.

</td>
</tr>
<tr>
<td valign="top">

**💨 Rooms, vents and leaks**<br>
Working vents puff colour, every room's edge is dotted in, and its leaks glow red.

</td>
<td valign="top">

**🔗 Links and areas**<br>
Dotted lines join hubs, monitors and panels to what they serve; stations' areas are outlined.

</td>
</tr>
</table>


## In detail

`/ezdb debug` gives you a **Debug Potion**. Drink it, and for 8 minutes:
- **every EZvac sign gets a coloured frame:** a ring of specks around its
  board, in a colour for its kind;
- **every piece of equipment glows** with an outline you can see through
  walls, so equipment hidden in a casing is easy to find: grey while it's
  stopped, cyan while it cools, orange while it heats;
- **working vents puff a cloud,** cyan or orange, with a column rising out of
  it, while their system cools or heats, and so does running equipment;
- **every room's edge is dotted in, and the openings it leaks through glow
  red** through walls (doorways, windows, holes), on EZvac BETA 1.0 Pilot 5
  and newer;
- **weather stations' areas** are outlined, and **dotted lines** join each
  sensor hub to its sensors, each RPM monitor to its equipment and each
  settings panel to its thermostat.

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
next door. On EZvac BETA 1.0 Pilot 5 and newer, its room's edge and leaks
show too.

> **EZvac BETA 1.0 Pilot 5 has a built-in EZdebug** that shows the system
> you're linking, with its room and leaks. EZdebug+ adds the Debug Potion
> (every system at once), stations' areas and the links. With EZdebug+
> installed, the built-in one steps aside.

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

<details>
<summary><b>How it works</b></summary>

<br>

EZdebug+ is built only on EZvac's public API: `devices(world)` lists every
device with its position, the equipment and thermostat views say what's
running, and the device events keep the outlines in step. `linkTool()` (API
1.3) says whether a player holds the link tool and which system it has
selected. Equipment outlines
are block displays that only debug players are shown; they're never saved
with the world. Frames and clouds are particles sent to debug players only.
See [EZvac's API guide](https://github.com/EZvac-Plugin/EZvac/blob/main/docs/api.md).

</details>

<details>
<summary><b>Building</b></summary>

<br>

```shell
mvn clean package
```

The jar is written to `target/EZdebugPlus-<version>.jar`.

</details>

## License

MIT; see [LICENSE](LICENSE).
