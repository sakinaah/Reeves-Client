# Reeves Client

Reeves Client is a modern Minecraft client built exclusively for Hypixel players, combining performance, customization, and quality-of-life features in a clean and professional package. Designed around the latest supported Minecraft versions, Reeves Client provides powerful SkyBlock utilities, PvP enhancements, performance optimizations, detailed statistics tracking, and an advanced customization system while maintaining strict compliance with Hypixel's rules.

From Dungeons and Garden progression to Bazaar tracking, Auction House analysis, session statistics, FPS improvements, and personalized cosmetics, Reeves Client is designed to be the all-in-one companion for every Hypixel experience.

Built with a modern Fabric-based architecture, Reeves Client prioritizes speed, stability, visual polish, and long-term support, delivering a premium experience without compromising fairness or player safety.

## Features

- **HUD System** — FPS, CPS, Ping, Coordinates, Direction, Armor, Potions, Keystrokes, Clock, Session Timer, Weather — all drag-and-drop repositionable
- **Performance** — Entity render controls, particle reducer, one-click optimization profiles
- **SkyBlock** — Skill tracker, slayer tracker, collection tracker, minion tracker, Garden dashboard, SkyBlock calendar & mayor, Bazaar browser, Auction House browser, profit calculators, goal tracker
- **Dungeons** — Room identifier, secret waypoints (informational), party stats, score tracker
- **PvP** — Custom crosshair, hit effect customization
- **Custom Capes** — Upload any image, automatic 64×32 normalization, multiple profiles, live preview *(world rendering temporarily disabled on 1.21.10 — see Known Limitations)*
- **Full Settings UI** — Searchable settings, category navigation, drag-and-drop HUD editor

## Compliance

Reeves Client is built with player safety as the top priority:

- No automation of any kind
- No macros, auto-clickers, or scripted actions
- No packet manipulation
- No hidden-information exploitation
- No aim assistance or reach modification
- Dungeon features are informational overlays only — all actions performed by the player

## Requirements

- Minecraft 1.21.10
- Fabric Loader 0.19.3+
- Fabric API
- Java 21

## Known Limitations

The 1.21.10 render-pipeline rewrite changed several internal APIs. Two features
are temporarily disabled while they are ported to the new rendering system:

- **Custom cape in-world rendering** — cape upload/management UI still works; the cape is not yet drawn on the player model.
- **3D world-space waypoint beacons** — waypoint data and the waypoint list work; the in-world beacon visuals are off.

All other features are fully functional.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.10.
2. Download the latest `reeves-client-*.jar` from the [Releases](../../releases) page.
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.10.
4. Place both jars in your `.minecraft/mods/` folder (or your launcher's mods folder).
5. Launch Minecraft and press **Right Shift** to open the Reeves Client menu.

## Building

```bash
./gradlew build
```

The compiled jar will be in `build/libs/`.

## Development Setup

1. Clone the repository
2. Run `./gradlew genSources` to generate Minecraft sources
3. Open in IntelliJ IDEA (File → Open → select the project root)
4. Run the `Minecraft Client` run configuration

## Configuration

All settings are stored in `.minecraft/config/reeves-client/`:

- `general.json` — Global settings (API key, accent color, etc.)
- `modules.json` — Per-module enable state and settings
- `hud.json` — HUD element positions, scales, and visibility
- `waypoints.json` — Saved custom waypoints
- `goals.json` — Personal SkyBlock goals

## Hypixel API Key

To use Bazaar, Auction House, and player statistics features, you need a Hypixel API key:

1. Join Hypixel and run `/api new`
2. Open Reeves Client settings → General → paste your API key

The key is stored locally and never shared.

## License

MIT License — see [LICENSE](LICENSE)

## Contributing

Pull requests welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting.

All contributions must follow the same compliance rules as the main client — no features that could provide unfair advantages or violate Hypixel's rules.
