# Changelog

All notable changes to EZdebug+ are recorded here.

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
