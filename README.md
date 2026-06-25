# Reeves Client

Reeves Client is a modern, open-source (MIT) Hypixel companion client for Fabric.
It focuses on quality-of-life, customization, and performance while staying
strictly within Hypixel's rules — every feature is informational and client-side,
with no automation of any kind.

Built on a clean, modular Fabric architecture for Minecraft 1.21.10.

## Features

### HUD
- FPS, CPS, Ping, Coordinates, Direction, Armor, Potions, Keystrokes, Clock,
  Session Timer, Weather — all drag-and-drop repositionable in the HUD editor.
- **Dungeon Tracker HUD** — floor, time, secrets, deaths, crypts, puzzles, blood-room
  timer, run-complete, estimated score/grade.
- **Dungeon Map overlay** — renders the in-game dungeon map item as a draggable overlay.
- Per-element theming, opacity, background toggle; **Reset All** to defaults.

### Appearance (unified theming)
- **Settings → Appearance**: colour presets (Midnight, Crimson, Emerald, Amethyst,
  Aqua, Light), accent-hue slider, panel-opacity slider, text-shadow toggle, live
  preview. Applies to the whole UI at once and persists.

### SkyBlock & Economy
- Skill / slayer / collection / minion trackers, Garden dashboard, SkyBlock calendar.
- **Economy tooltips** — Bazaar buy/sell prices in item tooltips (public API, no key
  required), cached and refreshed off-thread.
- Bazaar & Auction House browsers, profit calculators, goal tracker.

### Dungeons (informational only)
- Room identifier, secret-waypoint data, party stats, score/secrets tracking,
  blood-room timer, run completion.

### Item Protection
- Lock individual items (by SkyBlock id or vanilla id) to prevent accidental
  dropping / selling / salvaging. Hold the lock key (default **L**) and click an
  item to lock/unlock it. Locked items show a padlock and a tooltip line.

### PvP
- Custom crosshair, hit-effect customization, plus the FPS/CPS/Ping/Keystrokes/
  Armor/Potion HUDs above.

### Tools
- Item browser with registry search and an **inline calculator** (type `2.5m * 3`
  or `=(64*9)/64` in the search bar).
- Custom capes — upload an image, 64×32 normalization, multiple profiles, live
  preview *(see Known Limitations for in-world rendering)*.

### Performance
- Entity-render controls, particle reducer, one-click graphics profiles (applied
  once on change, not every tick), HUD ticks skipped while hidden, throttled
  SkyBlock/dungeon parsing, off-thread API caching.

## Keybinds (rebindable in Options → Controls)
- **Right Shift** — open the Reeves menu
- **L** — item lock (hold + click an item)
- HUD editor and other actions are accessible from the menu.

## Compliance
- No automation, macros, auto-clickers, or scripted actions.
- No packet manipulation, no hidden-information exploitation, no aim/reach changes.
- All dungeon/SkyBlock features are read-only overlays — the player performs every action.

## Known Limitations
The 1.21.10 render-pipeline rewrite changed several rendering APIs. Status:

- **3D world rendering** — re-implemented on Fabric's `WorldRenderEvents` (waypoint
  boxes). Pending final visual verification on live servers.
- **Custom cape in-world rendering** — upload/management UI works; the cape is not
  yet drawn on the player model.
- **In-world dungeon secret boxes / map→world room mapping** — needs live coordinate
  calibration; the on-screen dungeon HUD and map overlay work today.
- **Full JEI-style crafting recipes** — the item browser does search + item info;
  full recipe display depends on the reworked 1.21.10 recipe-display API and is not
  yet wired.

## Requirements
- Minecraft 1.21.10
- Fabric Loader 0.19.3+
- Fabric API
- Java 21

## Installation
1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.10.
2. Download the latest `reeves-client-*.jar` from [Releases](../../releases).
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.10.
4. Put both jars in your `mods/` folder.
5. Launch and press **Right Shift**.

## Building
```bash
./gradlew build   # requires JDK 21; output in build/libs/
```

## Configuration
Stored in `.minecraft/config/reeves-client/`:
- `general.json` — global settings, theme, API key
- `modules.json` — per-module enable state + settings (incl. item lock-list)
- `hud.json` — HUD positions/scales/visibility/theming
- `waypoints.json`, `goals.json`, `cape_config.json`

## Hypixel API Key (optional)
Bazaar tooltips work without a key (public endpoint). For Auction House and player
stats, set a key in Settings → General (`/api new` on Hypixel). Stored locally only.

## Credits & License
MIT License — see [LICENSE](LICENSE).

Some features are *inspired by* community mods (JEI, Skytils, SkyHanni) and were
implemented **clean-room** — see [ACKNOWLEDGEMENTS.md](ACKNOWLEDGEMENTS.md). No
third-party copyleft code is included.

## Contributing
PRs welcome — see [CONTRIBUTING.md](CONTRIBUTING.md). All contributions must follow
the same compliance rules (no unfair-advantage features) and must not paste source
from copyleft-licensed mods (describe behaviour and implement it fresh).
