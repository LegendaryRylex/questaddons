# Quest Addons

Editor conveniences for FTB Quests.

Laying out a quest book means a lot of repeated mouse work that FTB Quests puts behind fixed gestures or no gesture at all. This addon puts those on keybinds, all remappable from the vanilla Controls screen under "Quest Addons".

Selecting and moving quests happens on the left mouse button while a keybind is held, instead of the middle button, and panning gets out of the way while it is. Numpad Enter is accepted anywhere the quest book accepts Enter.

An open quest can also be turned straight into an FTB Filter System smart filter matching everything its item tasks ask for, handed to you in creative mode.

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
