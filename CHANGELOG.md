# Changelog

All notable changes to EZdebug+ are recorded here.

## 0.2.0

Debug vision while linking, for EZvac ALPHA 3.0's link tool. Still runs on
EZvac ALPHA 2.2 (API 1.2), without it.

### Added

- **Holding EZvac's link tool shows debug vision,** with no potion, for as
  long as it's in your hand. It needs EZvac ALPHA 3.0 or newer (API 1.3),
  and `ezdebug.use` or `ezvac.admin`. A running potion is unaffected: its
  timer keeps going, and milk ends the potion, not the tool's vision.
- **The selected system stands out** for the player holding the tool, in
  the new `colors.selected` (red by default):
  - its signs are framed in it;
  - its equipment gets a dotted box on top of its usual outline;
  - each of its vents gets a marker, even while idle, so every linked vent
    can be found.

  Everything else keeps its usual colours, so another house's system is easy
  to tell apart.
- **`link-tool: false`** in config.yml switches tool vision off.

## 0.1.0

The first release: the Debug Potion. Needs EZvac ALPHA 2.2 or newer (API 1.2).

### Added

- **`/ezdb debug [player]`** gives a Debug Potion (`ezdebug.use`, ops).
- **Drinking it** gives 8 minutes of debug vision, seen only by that player:
  - a coloured frame around every EZvac sign, coloured by kind;
  - glowing outlines on all equipment, visible through walls: grey stopped,
    cyan cooling, orange heating;
  - small clouds on the vents of systems that are cooling or heating, and on
    running equipment.
- **Ending it:** milk, death, the timer, or `/ezdb off`. The remaining time
  carries over a relog. Night Vision shows the countdown (`effect:` in
  config.yml).
- **config.yml:** the duration, radius, refresh rate, countdown effect and
  every colour.
