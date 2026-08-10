# Quest Box Select

Rebindable box-select drag for the FTB Quests editor.

FTB Quests only lets you box-select quests and images by holding the middle mouse button and dragging. This addon puts that gesture on a keybind instead: hold the key and drag with the left mouse button. It defaults to Alt and can be remapped from the vanilla Controls screen, under "Quest Box Select".

While the keybind is held, left-drag no longer pans the quest board. The original middle-mouse drag is left untouched.

Client-side only, and only active in editing mode.

NeoForge mod for Minecraft 1.21.1. Requires FTB Quests.

## Building

```
.\gradlew build
```

Jar lands in `build\libs\`.

## Development

- `.\gradlew runClient` / `runServer` / `runData` / `runGameTestServer`
- `.\gradlew spotlessApply` before committing
