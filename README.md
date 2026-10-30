# Quest Addons

Editor conveniences for FTB Quests.

Laying out a quest book means a lot of repeated mouse work that FTB Quests puts behind fixed gestures or no gesture at all. This addon puts those on keybinds, all remappable from the vanilla Controls screen under "Quest Addons". Anything FTB Quests now does natively has been dropped in its favour.

## Keybinds

| Keybind | Default | What it does |
|---|---|---|
| Move Selection | Left Shift | Hold and left-click a quest or chapter image to pick up the selection and move it, adding the clicked object if it was not selected. Images with Lock Position set stay put. |
| Toggle Optional | O | Press while hovering a task to make it optional or required, or hold and right-click a quest to toggle the quest. |
| Smart Filter From Quest Items | Numpad + | In an open quest, gives an FTB Filter System smart filter matching everything its item tasks ask for. In your inventory, matches the hotbar, or the whole inventory with Shift. Creative mode only. |
| Split View | V | Opens a second chapter side by side with the first. Both halves are live and editable, and the split survives recipe viewer round trips. Press again to close it. |
| Split View Stacked | Alt + V | The same, with one chapter above the other. |

Editing keybinds need edit mode, and the Smart Filter needs FTB Filter System installed.

## Other changes

- Placing or moving quests always snaps to a 0.5 grid, whatever the quest book's Grid Scale is set to.
- Numpad Enter is accepted anywhere the quest book accepts Enter.
- Tooltips on quest context menus draw in front of the menu instead of behind it.
- Right-clicking a selected quest offers Change Shape for all, which opens a sub-menu of every shape, the current one ticked; picking one applies it to every selected quest.
- The quest description editor gets a JSON Fixer button next to Convert to JSON (Alt + F). It repairs every broken JSON text line in the description: missing or extra brackets, trailing or missing commas, single or curly quotes, unquoted keys, stray quotes inside text, miscased colour names, string booleans, and click and hover events written in the newer snake_case format. Lines it cannot repair are listed and the cursor jumps to the first one.
- The quest description editor gets an Insert Keybind button next to the JSON Fixer. It opens a searchable dropdown of every keybind in the game, bound or not, and adds the one you pick as a JSON keybind entry at the cursor. It shows each player their own key in yellow, with the default key on hover. On a plain text line the line becomes JSON with its colours kept, and the selection, if any, is replaced.

Client-side only. Nothing is required on the server; edits go through FTB Quests' own editor packets.

NeoForge mod for Minecraft 1.21.1. Requires FTB Quests 2101.1.36 or newer.
