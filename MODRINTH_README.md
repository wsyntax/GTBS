# Guess The Build Solver

A Fabric client mod that auto-solves **Guess The Build** on Hypixel.

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

## Requirements

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for settings)

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

## Word List

The mod ships with a curated word list of **1,944 words** commonly used in Hypixel's Guess The Build minigame, including animals, objects, buildings, vehicles, compound words, and more.

## Troubleshooting

| Problem | Solution |
|---------|----------|
| No matches detected | Make sure you're on the Hypixel server and playing Guess The Build |
| Mod doesn't load | Verify Fabric API and Fabric Loader versions match your MC version |
| Overlay doesn't appear | Press Right Shift only after a hint is shown in chat/action bar |

## Links

- [GitHub](https://github.com/wsyntax/GTBS)
- [Issues](https://github.com/wsyntax/GTBS/issues)
