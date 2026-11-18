## Keybinds

| Keybind | Default | What it does |
| Toggle Optional | O | Press while hovering a task to make it optional or required, or hold and right-click a quest to toggle the quest. |
| Smart Filter From Quest Items | Numpad + | In an open quest, gives an FTB Filter System smart filter matching everything its item tasks ask for. In your inventory, matches the hotbar, or the whole inventory with Shift. Creative mode only. |
| Split View | V | Opens a second chapter side by side with the first. Both halves are live and editable, and the split survives recipe viewer round trips. Press again to close it. |
| Split View Stacked | Alt + V | The same, with one chapter above the other. |

## Other changes

- Placing or moving quests always snaps to a 0.5 grid, whatever the quest book's Grid Scale is set to.
- Numpad Enter is accepted anywhere the quest book accepts Enter.
- The quest object picker also searches by ID, with or without the leading `#`.
- Right-clicking a selected quest offers Change Shape for all, which opens a sub-menu of every shape, the current one ticked; picking one applies it to every selected quest.
- The quest description editor gets insert buttons next to the JSON Fixer: Keybind, Hover Text (1.21.1 only, native in 26.1.2), Item, Command Link, Copy Link and Font, plus GuideMe Link and Patchouli Link when those mods are installed. Each adds its element as JSON at the cursor, wrapping or replacing the selection. Keybinds show each player their own key in yellow, and clicking an item opens its recipes in JEI, REI or EMI. Item recipes and docs links need FTB XMod Compat.
- The quest description editor gets a JSON Fixer button next to Convert to JSON (Alt + F). It repairs every broken JSON text line in the description: missing or extra brackets, trailing or missing commas, single or curly quotes, unquoted keys, stray quotes inside text, miscased colour names, string booleans, and click and hover events written in the newer snake_case format. Lines it cannot repair are listed and the cursor jumps to the first one.
- The quest description editor gets a Live Preview button (Alt + V) that splits the editor in two, with the text on the left and the description on the right as players will see it. The preview updates as you type, follows the page the cursor is on, shows hover text, and wraps lines where the quest window does whenever the pane is wide enough. The divider between the halves drags to resize.
- Arrow-key move: arrow keys move the selected quests without picking them up first (QuestScreenArrowMoveMixin.java).
- ID search in the quest object picker, with or without #.
- Lock Position respected: Move Selection skips chapter images with Lock Position set. On 26.1.2 it picks them up.
- Retyping a selection: when you type exactly the text that's selected in an FTB text box, the selection collapses instead of being replaced (TextBoxRetypeSelectionMixin.java).

## Keybinds

| Keybind | Default | What it does |
|---|---|---|
| Box Select | Left Shift | Hold and drag to select quests with a box. |
| Move Selection | Left Shift | Hold and left-click a quest or chapter image to pick up the selection and move it, adding the clicked object if it was not selected. |
| Complete Instantly | C | Hold and click a quest to complete it. |
| Reset Progress | R | Hold and click a quest to reset its progress. |
| Delete | F | Hold and click a quest or chapter image to delete it. |
| Toggle Optional | O | Press while hovering a task to make it optional or required, or hold and right-click a quest to toggle the quest. |
| Smart Filter From Quest Items | Numpad + | In an open quest, gives an FTB Filter System smart filter matching everything its item tasks ask for. In your inventory, matches the hotbar, or the whole inventory with Shift. Creative mode only. |
| Save Quests on Server | Ctrl + S | Saves the quest book on the server, or downloads the quest files instead with Shift held. |
| Split View | V | Opens a second chapter side by side with the first. Both halves are live and editable, and the split survives recipe viewer round trips. Press again to close it. |
| Split View Stacked | Alt + V | The same, with one chapter above the other. |
