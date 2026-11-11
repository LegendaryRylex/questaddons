package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.client.gui.MultilineTextEditorScreen;
import dev.rylex.questaddons.compat.guideme.GuideMeCompat;
import dev.rylex.questaddons.compat.patchouli.PatchouliCompat;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

public enum DescriptionInsert {
    KEYBIND("keybind", () -> Icons.KEYBOARD) {
        @Override
        public void open(InsertContext context) {
            context.menu(KeybindMenu.items(context::put));
        }
    },
    TRANSLATION("translation", () -> Icons.GLOBE) {
        @Override
        public void open(InsertContext context) {
            context.prompt(prompt(), "", key -> context.put(Component.translatable(key)));
        }
    },
    ITEM("item", () -> Icons.DIAMOND) {
        @Override
        public void open(InsertContext context) {
            context.pickItem(stack -> context.put(context.label(stack.getHoverName())
                    .withStyle(style -> style.withHoverEvent(
                            new HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(stack))))));
        }
    },
    COMMAND("command", () -> ItemIcon.ofItem(Items.COMMAND_BLOCK)) {
        @Override
        public void open(InsertContext context) {
            context.prompt(prompt(), "/", typed -> {
                String command = typed.startsWith("/") ? typed : "/" + typed;
                context.put(context.label(command).withStyle(style -> style.withUnderlined(true)
                        .withClickEvent(new ClickEvent.RunCommand(command))));
            });
        }
    },
    COPY("copy", () -> Icons.NOTES) {
        @Override
        public void open(InsertContext context) {
            context.prompt(
                    prompt(),
                    context.selection(),
                    value -> context.put(context.label(value).withStyle(style -> style.withUnderlined(true)
                            .withClickEvent(new ClickEvent.CopyToClipboard(value))
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click"))))));
        }
    },
    FONT("font", () -> Icons.FEATHER) {
        @Override
        public void open(InsertContext context) {
            context.menu(fontItems(font -> context.put(context.label(PLACEHOLDER)
                    .withStyle(style -> style.withFont(new FontDescription.Resource(font))))));
        }
    },
    GUIDEME("guideme", () -> Icons.BOOK, GuideMeCompat::isLoaded) {
        @Override
        public void open(InsertContext context) {
            context.menu(docsItems(GuideMeCompat.links(), context));
        }
    },
    PATCHOULI("patchouli", () -> Icons.BOOK_RED, PatchouliCompat::isLoaded) {
        @Override
        public void open(InsertContext context) {
            context.menu(docsItems(PatchouliCompat.links(), context));
        }
    };

    private static final String PLACEHOLDER = "EDIT HERE";
    private static final String FONT_SAMPLE = "  Aa Bb Cc";
    private static final String FONT_FOLDER = "font/";
    private static final String JSON = ".json";
    private static final String DOCS_LOCATION = "location";

    private final String name;
    private final Supplier<Icon> icon;
    private final BooleanSupplier available;

    DescriptionInsert(String name, Supplier<Icon> icon) {
        this(name, icon, () -> true);
    }

    DescriptionInsert(String name, Supplier<Icon> icon, BooleanSupplier available) {
        this.name = name;
        this.icon = icon;
        this.available = available;
    }

    public abstract void open(InsertContext context);

    public boolean isAvailable() {
        return available.getAsBoolean();
    }

    public Icon icon() {
        return icon.get();
    }

    public Component title() {
        return Component.translatable(key());
    }

    public Component tooltip() {
        return Component.translatable(key() + ".tooltip");
    }

    Component prompt() {
        return Component.translatable(key() + ".prompt");
    }

    private String key() {
        return "questaddons.gui.insert_" + name;
    }

    private static List<ContextMenuItem> fontItems(Consumer<Identifier> picked) {
        return Minecraft.getInstance()
                .getResourceManager()
                .listResources("font", file -> file.getPath().endsWith(JSON))
                .keySet()
                .stream()
                .map(file -> Identifier.fromNamespaceAndPath(
                        file.getNamespace(),
                        file.getPath()
                                .substring(FONT_FOLDER.length(), file.getPath().length() - JSON.length())))
                .filter(font -> !font.getPath().contains("/"))
                .sorted()
                .map(font -> new ContextMenuItem(
                        Component.literal(font.toString())
                                .append(Component.literal(FONT_SAMPLE)
                                        .withStyle(style -> style.withFont(new FontDescription.Resource(font)))),
                        Icon.empty(),
                        button -> picked.accept(font)))
                .toList();
    }

    private static List<ContextMenuItem> docsItems(List<DocsLink> links, InsertContext context) {
        if (links.isEmpty()) {
            return List.of(new ContextMenuItem(
                    Component.translatable("questaddons.gui.insert_docs.none"), Icon.empty(), button -> {}));
        }
        return links.stream()
                .map(link -> new ContextMenuItem(
                        MenuLabel.of(link.title(), link.source()),
                        Icon.empty(),
                        button -> context.put(context.label(link.title())
                                .withStyle(style -> style.withUnderlined(true).withClickEvent(docsClick(link))))))
                .toList();
    }

    private static ClickEvent docsClick(DocsLink link) {
        CompoundTag payload = new CompoundTag();
        payload.putString(DOCS_LOCATION, link.value());
        return new ClickEvent.Custom(MultilineTextEditorScreen.OPEN_DOCS_ACTION, Optional.of(payload));
    }
}
