# Quest Addons

Editor tools for FTB Quests. Client-side only: edits go through FTB Quests' own editor packets, so nothing is needed on the server.

Requires NeoForge for Minecraft 26.1.2 and FTB Quests 26.1.2.6 or newer.

## Keybinds

All unbound by default. Set them in Controls under "Quest Addons". Editing keybinds need edit mode.

| Keybind | What it does |
|---|---|
| Box Select | Hold and drag to select quests with a box. |
| Move Selection | Hold and left-click a quest or chapter image to pick up the selection and move it. The clicked object joins the selection. |
| Toggle Optional | Press over a task to make it optional or required. Hold and right-click a quest to toggle the quest. |
| Smart Filter | Gives an FTB Filter System smart filter matching an open quest's item tasks, or your hotbar from the inventory (the whole inventory with Shift). Creative only, needs FTB Filter System. |
| JSON Fixer | Runs the description editor's JSON Fixer. |
| Live Preview | Shows or hides the description editor's Live Preview. |
| Insert Keybind, Hover Text, Item or Fluid, Command Link, Copy Link, Font, Inline Image, Side Image, Recipe Link, GuideMe Link, Patchouli Link | Opens that insert button in the description editor. Recipe Link needs JEI, GuideMe Link and Patchouli Link need their mod. |

## Quest book

- Placing or moving quests snaps to a 0.5 grid, whatever the Grid Scale.
- Numpad Enter works wherever Enter does.
- A Search Quests button in the quest book's side buttons opens the quest search, alongside its keybind.
- The quest object picker also searches quest descriptions and subtitles, matching every word you type in any order.
- With quests selected and no quest open, the arrow keys move the selection without picking it up first.
- Right-clicking a selected quest offers Change Shape for all, which sets one shape on every selected quest.

## Description editor

- **JSON Fixer**, next to Convert to JSON, repairs every broken JSON text line: missing or extra brackets, trailing or missing commas, single or curly quotes, unquoted keys, stray quotes in text, miscased colour names, string booleans, click and hover events in the older camelCase format, text holding a pasted copy of its own element, click and hover events that do nothing, and a link, tooltip, shift-click text or style split across two copies of the same text. A link or tooltip on empty text moves to the text after it. Formatting codes inside JSON text, such as `&d` or `&#FF8800`, become JSON styles. Lines it can't repair are listed and the cursor jumps to the first.
- **Insert buttons** add a text element as JSON at the cursor, wrapping or replacing the selection, also inside a JSON element's text, where only the selected part takes the new style.
  - Keybind: each player sees their own key in yellow.
  - Item or Fluid: pick either from one list, with JEI, Items, Fluids and Inventory search modes. The JEI mode lists only what JEI shows, so hidden and removed items stay out of the search. Adds its name with its tooltip. The tooltip shows its icon, and clicking opens its recipes in JEI, REI or EMI (needs FTB XMod Compat).
  - Command Link and Copy Link: a link that runs a command or copies text.
  - Font: any loaded font.
  - Recipe Link (JEI): pick an item or fluid, then one recipe that makes it, shown with its machine's icon and recipe ID. A bucket also lists the recipes that make its fluid. Adds the name as a link to that recipe, with the recipe type on hover.
  - GuideMe Link and Patchouli Link: pick a page or entry to add a `docs:` link to it (needs FTB XMod Compat).
- **Live Preview** splits the editor, with the text on the left and the rendered description on the right at quest window width. It updates as you type, follows the cursor's page and shows hover text. It opens shown; turn off Open Live Preview with the Editor under Quest Addons in FTB Quests' client settings to start hidden.

## Images in text

- An `{image:...}` tag on a line with text sits inline like a word, and the line grows to fit it. Several can share a line. It works inside JSON text as `{"text": "{image:...}"}`.
- `align:left` or `align:right` puts it beside its paragraph, with the text wrapping around it.
- `valign:top`, `valign:center` or `valign:bottom` sets a side image's position in its paragraph.
- `keep_aspect:true` fits the image inside its width and height without stretching it.
- `{image:item:minecraft:diamond ...}` draws an item or block as it looks in an inventory, with its tooltip on hover and its recipes on click.
- Hover text and click actions work as they do on a whole-line image.
- Insert Inline Image adds an image, item or block at the cursor at text height. Insert Side Image puts one left or right of the cursor's paragraph, at its top, middle or bottom, and adds placeholder text on an empty line.

## Quest windows

- Dragging a quest window by its title bar floats it. Floating windows stay open while you use the book, so several quests can be open at once. Each has its own close button, clicking one brings it to the front, and they reopen where you left them.
- The lock button in the header floats a docked window in place, or docks a floating one.
- The Collapse button shrinks a window to its title, tasks and rewards. Collapsed quests stay collapsed when reopened.
- Picking a quest from View Dependants opens it in a floating window beside the current one.

## Dependencies

- Right-clicking the View Dependencies arrow moves the window to the right edge and outlines its dependencies: quests in the current chapter, the chapter list entry of another chapter holding one, or the chapter group when it is collapsed. Right-click again to clear.
- A quest with unfinished dependencies opens with a Dependencies panel listing each one with its icon and tasks. Left-clicking the arrow shows or hides it.
- With more than one dependency, a Show button switches the list between Pending, Completed and All, and its tooltip says whether all or only some are needed. It starts on Pending when the panel opens by itself, and on All when you open it.
- Done dependencies are ticked and shrink to their title. Clicking a dependency or one of its tasks opens it in a floating window, and a chapter dependency switches to that chapter. Quests that hide their details show no tasks, and hidden quests are only counted.

## Pinned Quests

- Shows each quest's and task's icon beside its progress.
- Drags by its title bar in the quest book, any container screen and JEI's recipe screens. The spot you pick is used everywhere, over the world too. Right-clicking the title bar resets it.
- Clicking a quest opens it, and clicking a task does what it does in the quest window. JEI's keys work on a pinned item task. Items its unfinished tasks ask for are highlighted in an open container.
- With NEI Features for JEI, its keys work on a pinned item task and any item in the quest book: bookmark (A), chat link (Ctrl + L), pull from containers (V), autocraft (Shift + C), and copy the name, ID or tags (Ctrl + C, Ctrl + X, Ctrl + D). On a pinned task, Ctrl + A, Ctrl + L and V use the amount the task still needs. In edit mode the quest book keeps its own Ctrl keys.
