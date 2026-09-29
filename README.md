# Quest Addons

Editor conveniences for FTB Quests.

Laying out a quest book means a lot of repeated mouse work that FTB Quests puts behind fixed gestures or no gesture at all. This addon puts those on keybinds, all remappable from the vanilla Controls screen under "Quest Addons".

Selecting and moving quests happens on the left mouse button while a keybind is held, instead of the middle button, and panning gets out of the way while it is. Numpad Enter is accepted anywhere the quest book accepts Enter.

Right-clicking a selected quest offers Change Shape for all, which cycles every selected quest's shape: left-click for the next shape, right-click for the previous, with the menu staying open.

An open quest can also be turned straight into an FTB Filter System smart filter matching everything its item tasks ask for, handed to you in creative mode.

The quest description editor gets a JSON Fixer button next to Convert to JSON (Alt + F). It repairs every broken JSON text line in the description: missing or extra brackets, trailing or missing commas, single or curly quotes, unquoted keys, stray quotes inside text, miscased colour names and string booleans. Click and hover events written in the pre-1.21.5 camelCase format are converted, and old change_page quest links and docs: links become FTB Quests link events. Lines it cannot repair are listed and the cursor jumps to the first one.

Client-side only. Nothing is required on the server, and the quest file is never modified.

NeoForge mod for Minecraft 26.1.2. Requires FTB Quests.

## Building

```
.\gradlew build
```

Jar lands in `build\libs\`.

## Development

- `.\gradlew runClient` / `runServer` / `runData` / `runGameTestServer`
- `.\gradlew spotlessApply` before committing
