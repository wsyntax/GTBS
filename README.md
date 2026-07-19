<p align="center">
  <img src="src/main/resources/icon.png" width="128" height="128" alt="Guess The Build icon">
</p>

<h1 align="center">Guess The Build Solver</h1>

<p align="center">
  A Fabric client mod that auto-solves <strong>Guess The Build</strong> on Hypixel.
</p>

<p align="center">
  <a href="../../releases"><img src="https://img.shields.io/github/v/release/wsyntax/GTBS?style=flat-square&label=Latest%20Release" alt="Latest Release"></a>
  <img src="https://img.shields.io/github/license/wsyntax/GTBS?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/Minecraft-1.20.x--26.x-brightgreen?style=flat-square" alt="Minecraft Versions">
  <img src="https://img.shields.io/badge/Mod%20Loader-Fabric-blue?style=flat-square" alt="Fabric">
</p>

---

## Features

- **Instant Word Matching** - Matches build hints against a 1,900+ word dictionary in real-time
- **Auto-Send** - Automatically submits the answer when only one word matches
- **Click-to-Send Overlay** - Hover over matched words and click to send instantly
- **Modern HUD** - Animated overlay with smooth transitions, staggered entry, and accent glow effects
- **Legacy Mode** - Clean, minimal Minecraft-style panel for low-end systems
- **Configurable Delay** - Set auto-send delay from 5 to 120 ticks
- **Sound Effects** - Plays a notification sound when matches are found
- **Mod Menu** - Full settings integration via Mod Menu
- **Toast Notifications** - Non-intrusive popup confirmations
- **Settings GUI** - In-game configuration with sliders and toggles

## Supported Versions

| Minecraft | Branch | Java |
|-----------|--------|------|
| 1.20.1 | `1.20.1` | 17 |
| 1.20.4 | `1.20.4` | 17 |
| 1.20.6 | `1.20.6` | 21 |
| 1.21.1 | `1.21.1` | 21 |
| 1.21.4 | `1.21.4` | 21 |
| 1.21.5 | `1.21.5` | 21 |
| 1.21.8 | `1.21.8` | 21 |
| 1.21.11 | `1.21.11` | 21 |
| 26.1.2 | `main` | 25 |
| 26.2 | `26.2` | 25 |

Switch branches to download the version you need:
```bash
git clone -b 1.21.4 https://github.com/wsyntax/GTBS.git
```

## Requirements

- [Fabric Loader](https://fabricmc.net/) 0.16.9+
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for settings)

Exact dependency versions are listed in `gradle.properties` for each branch.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) for your Minecraft version
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in `.minecraft/mods/`
3. Download [Mod Menu](https://modrinth.com/mod/modmenu) and place it in `.minecraft/mods/` (optional)
4. Download the latest `.jar` from [Releases](../../releases) matching your MC version
5. Drop the `.jar` into `.minecraft/mods/`
6. Launch Minecraft and join a Hypixel Guess The Build game

## Usage

| Keybind | Action |
|---------|--------|
| **Right Shift** | Open word overlay (when matches are detected) |
| **F8** | Open settings |
| Mod Menu | Access settings from the mods list |

### How It Works

1. Join a Guess The Build game on Hypixel
2. When a hint appears (e.g., `_ _ _ _ _ _`), the mod automatically detects it from chat/action bar
3. If only **one word** matches the pattern, it sends the answer automatically (if enabled)
4. If **multiple words** match, press **Right Shift** to see the overlay with all possibilities
5. Click any word in the overlay to send it to chat

## Configuration

Open settings with **F8** or via **Mod Menu**:

| Setting | Default | Description |
|---------|---------|-------------|
| Instant Send | Off | Send immediately when 1 match found |
| Paste In Chat | Off | Paste into chat bar instead of sending directly |
| Auto Send | On | Auto-submit when only 1 match remains |
| Auto Send Delay | 40 ticks | Delay before auto-sending (5-120 ticks) |
| Show Hints | On | Display hint in the overlay |
| Overlay Animations | On | Smooth open/close/entry animations |
| Sound Effects | On | Play sound on match detection |
| Legacy Overlay | Off | Simple Minecraft-style panel |
| Overlay Opacity | 80% | Background opacity of the overlay |

## Building from Source

```bash
git clone https://github.com/wsyntax/GTBS.git
cd GTBS
git checkout <your-mc-version>   # e.g. 1.21.4
./gradlew build
```

The built jar will be in `build/libs/`.

## Word List

The mod ships with a curated word list of **1,944 words** commonly used in Hypixel's Guess The Build minigame, including:

- Animals, objects, buildings, vehicles
- Compound words and multi-word phrases
- Pop culture references and game-specific terms

You can customize the word list by editing `wordlist.txt` in the mod's resources before building.

## Troubleshooting

| Problem | Solution |
|---------|----------|
| No matches detected | Make sure you're on the Hypixel server and playing Guess The Build |
| Mod doesn't load | Verify Fabric API and Fabric Loader versions match your MC version |
| Overlay doesn't appear | Press Right Shift only after a hint is shown in chat/action bar |
| Build fails | Make sure you have the correct Java version installed for your MC version |

## License

[MIT](LICENSE) - Made by Syntax
