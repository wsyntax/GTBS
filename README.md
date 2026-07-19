# Guess The Build Solver

A Fabric client mod that auto-solves **Guess The Build** on Hypixel.

## Features
- Matches build hints against a wordlist and shows possible answers
- Auto-send when only 1 word matches
- Overlay with click-to-send word list
- Configurable delay, opacity, animations
- Legacy overlay mode (plain Minecraft-style)
- Mod Menu integration

## Requirements
- Minecraft **26.1.2**
- Fabric Loader **0.19.3+**
- Fabric API **0.155.2+**

## Installation
1. Install Fabric Loader and Fabric API
2. Download the latest `.jar` from [Releases](../../releases)
3. Drop the `.jar` into your `.minecraft/mods` folder
4. Launch Minecraft

## Usage
- **Right Shift** — Open the word overlay (when words are detected)
- **F8** — Open settings
- Settings can also be accessed via **Mod Menu**

## Building from source
```bash
./gradlew build
```
The jar will be in `build/libs/`.

## License
MIT
