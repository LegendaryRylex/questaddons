package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.rylex.questaddons.compat.guideme.GuideMeCompat;
import dev.rylex.questaddons.compat.jei.JeiCompat;
import dev.rylex.questaddons.compat.patchouli.PatchouliCompat;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
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
            context.menu(List.of(
                    choice(context, "questaddons.gui.insert_inline_image.image", INLINE_HEIGHT, null, false),
                    choice(context, "questaddons.gui.insert_inline_image.item", INLINE_HEIGHT, null, true)));
        }
    },
    SIDE_IMAGE("side_image", () -> Icons.CAMERA) {
        @Override
        public void open(InsertContext context) {
            context.menu(List.of(
                    choice(context, "questaddons.gui.insert_side_image.left", SIDE_HEIGHT, "left", false),
                    choice(context, "questaddons.gui.insert_side_image.item_left", SIDE_HEIGHT, "left", true),
                    choice(context, "questaddons.gui.insert_side_image.right", SIDE_HEIGHT, "right", false),
                    choice(context, "questaddons.gui.insert_side_image.item_right", SIDE_HEIGHT, "right", true)));
        }
    },
    RECIPE("recipe", () -> ItemIcon.getItemIcon(Items.CRAFTING_TABLE), JeiCompat::isLoaded) {
        @Override
        public void open(InsertContext context) {
            context.menu(JeiCompat.recipeTargets().stream()
                    .map(target -> new ContextMenuItem(
                            MenuLabel.of(target.name().getString(), target.id()),
                            target.icon(),
                            button -> context.menu(recipeItems(target, context))))
                    .toList());
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
    private static final int RECIPE_ID_WIDTH = 300;
    private static final List<String> VALIGNS = List.of("top", "center", "bottom");

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

    public String id() {
        return name;
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

    private static ContextMenuItem choice(
            InsertContext context, String label, int height, @Nullable String align, boolean item) {
        Icon icon = item ? ItemIcon.getItemIcon(Items.GRASS_BLOCK) : Icons.ART;
        return new ContextMenuItem(Component.translatable(label), icon, button -> {
            if (align == null) {
                pick(context, height, null, null, item);
                return;
            }
            context.menu(VALIGNS.stream()
                    .map(valign -> new ContextMenuItem(
                            Component.translatable("questaddons.gui.insert_side_image.valign." + valign),
                            Icon.empty(),
                            choose -> pick(context, height, align, valign, item)))
                    .toList());
        });
    }

    private static void pick(
            InsertContext context, int height, @Nullable String align, @Nullable String valign, boolean item) {
        if (item) {
            context.pickItem(stack -> context.putImage(
                    ImageInsertion.tag(
                            "item:" + BuiltInRegistries.ITEM.getKey(stack.getItem()), height, height, align, valign),
                    align != null,
                    PLACEHOLDER));
        } else {
            context.pickImage(
                    image -> context.putImage(imageTag(image, height, align, valign), align != null, PLACEHOLDER));
        }
    }

    private static String imageTag(
            ResourceLocation image, int height, @Nullable String align, @Nullable String valign) {
        int[] size = TextureSize.of(Icon.getIcon(image.toString())).orElse(new int[] {1, 1});
        int width = Math.max(1, (int) Math.round((double) height * size[0] / size[1]));
        return ImageInsertion.tag(image.toString(), width, height, align, valign);
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

    private static List<ContextMenuItem> recipeItems(RecipeTarget target, InsertContext context) {
        List<RecipeLink> links = JeiCompat.recipesMaking(target);
        if (links.isEmpty()) {
            return List.of(new ContextMenuItem(
                    Component.translatable("questaddons.gui.insert_recipe.none"), Icon.empty(), button -> {}));
        }
        return links.stream()
                .map(link -> new ContextMenuItem(
                        MenuLabel.of(link.category().getString(), link.recipe(), RECIPE_ID_WIDTH),
                        link.catalyst().isEmpty() ? Icon.empty() : ItemIcon.getItemIcon(link.catalyst()),
                        button -> context.put(context.label(target.name()).withStyle(style -> style.withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.value()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, link.category()))))))
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
