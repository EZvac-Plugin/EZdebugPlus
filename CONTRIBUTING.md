# Contributing to EZdebug+

Bug reports, ideas and pull requests are welcome.

## Requirements

- Paper 1.21.1 (or a fork such as Purpur) with EZvac ALPHA 2.1 Pilot 4 or newer
- Java 21
- Maven 3.9 or newer

## Build and test

```shell
mvn clean package
```

This compiles, runs the tests, and writes `target/EZdebugPlus-<version>.jar`.
Every push and pull request is built and tested by GitHub Actions.

## How it fits together

- **The code only touches EZvac through its public API** (`ezvac-api`, from JitPack). Never reach into EZvac's internals: that is what keeps EZdebug+ working across EZvac releases.
- **The pure, unit-tested logic is in `model/`:**
  - `Palette` picks the colour for a device and what it's doing;
  - `Activity` says which vents and equipment are at work;
  - `DebugSettings` reads and checks config.yml.

  Add tests there when you change them.
- **The Bukkit side:**
  - `DebugPotion`: the item, drinking it, milk, death, relogs, and EZvac's device events;
  - `DebugVision`: who has debug vision and the redraw task;
  - `Outlines`: the glowing block displays, shown only to debug players;
  - `Commands`.
