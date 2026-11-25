package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.rylex.questaddons.compat.guideme.GuideMeCompat;
import dev.rylex.questaddons.compat.patchouli.PatchouliCompat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public enum DescriptionInsert {
    KEYBIND("keybind", () -> Icons.KEYBOARD) {
        @Override
        public void open(InsertContext context) {
            context.menu(KeybindMenu.items(context::put));
        }
    },
    HOVER_TEXT("hover_text", () -> Icons.INFO) {
        @Override
        public void open(InsertContext context) {
            context.prompt(
                    prompt(),
                    "",
                    tooltip -> context.put(context.label(PLACEHOLDER)
                            .withStyle(style -> style.withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tooltip))))));
        }
    },
    ITEM("item", () -> Icons.DIAMOND) {
        @Override
        public void open(InsertContext context) {
            context.pickItem(stack -> context.put(context.label(stack.getHoverName())
                    .withStyle(style -> style.withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack))))));
        }
    },
    COMMAND("command", () -> ItemIcon.getItemIcon(Items.COMMAND_BLOCK)) {
        @Override
        public void open(InsertContext context) {
            context.prompt(prompt(), "/", typed -> {
                String command = typed.startsWith("/") ? typed : "/" + typed;
                context.put(context.label(command).withStyle(style -> style.withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))));
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
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value))
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click"))))));
        }
    },
    FONT("font", () -> Icons.FEATHER) {
        @Override
        public void open(InsertContext context) {
            context.menu(fontItems(
                    font -> context.put(context.label(PLACEHOLDER).withStyle(style -> style.withFont(font)))));
        }
    },
    INLINE_IMAGE("inline_image", () -> Icons.ART) {
        @Override
        public void open(InsertContext context) {
            context.pickImage(image -> context.putImage(imageTag(image, INLINE_HEIGHT, null), false, PLACEHOLDER));
        }
    },
    SIDE_IMAGE("side_image", () -> Icons.CAMERA) {
        @Override
        public void open(InsertContext context) {
            context.menu(List.of(sideItem(context, "left", Icons.LEFT), sideItem(context, "right", Icons.RIGHT)));
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
    private static final int INLINE_HEIGHT = 8;
    private static final int SIDE_HEIGHT = 32;
    private static final int PNG_HEADER = 24;

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

    private static ContextMenuItem sideItem(InsertContext context, String side, Icon icon) {
        return new ContextMenuItem(
                Component.translatable("questaddons.gui.insert_side_image." + side),
                icon,
                button -> context.pickImage(
                        image -> context.putImage(imageTag(image, SIDE_HEIGHT, side), true, PLACEHOLDER)));
    }

    private static String imageTag(ResourceLocation image, int height, @Nullable String align) {
        int[] size = pixelSize(image);
        int width = Math.max(1, (int) Math.round((double) height * size[0] / size[1]));
        return ImageInsertion.tag(image.toString(), width, height, align);
    }

    private static int[] pixelSize(ResourceLocation image) {
        Optional<Resource> resource =
                Minecraft.getInstance().getResourceManager().getResource(image);
        if (resource.isEmpty()) {
            return new int[] {1, 1};
        }
        try (InputStream stream = resource.get().open()) {
            byte[] header = stream.readNBytes(PNG_HEADER);
            if (header.length < PNG_HEADER) {
                return new int[] {1, 1};
            }
            ByteBuffer buffer = ByteBuffer.wrap(header);
            int width = buffer.getInt(16);
            int height = buffer.getInt(20);
            return width > 0 && height > 0 ? new int[] {width, height} : new int[] {1, 1};
        } catch (IOException e) {
            return new int[] {1, 1};
        }
    }

    private static List<ContextMenuItem> fontItems(Consumer<ResourceLocation> picked) {
        return Minecraft.getInstance()
                .getResourceManager()
                .listResources("font", file -> file.getPath().endsWith(JSON))
                .keySet()
                .stream()
                .map(file -> ResourceLocation.fromNamespaceAndPath(
                        file.getNamespace(),
                        file.getPath()
                                .substring(FONT_FOLDER.length(), file.getPath().length() - JSON.length())))
                .filter(font -> !font.getPath().contains("/"))
                .sorted()
                .map(font -> new ContextMenuItem(
                        Component.literal(font.toString())
                                .append(Component.literal(FONT_SAMPLE).withStyle(style -> style.withFont(font))),
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
                        button -> context.put(context.label(link.title()).withStyle(style -> style.withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.value()))))))
                .toList();
    }
}
