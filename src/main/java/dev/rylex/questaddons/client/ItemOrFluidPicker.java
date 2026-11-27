package dev.rylex.questaddons.client;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.ResourceConfigValue;
import dev.ftb.mods.ftblibrary.config.ui.resource.ResourceSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.resource.ResourceSelectorScreen;
import dev.ftb.mods.ftblibrary.config.ui.resource.SearchModeIndex;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectableResource;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.SearchTerms;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.rylex.questaddons.compat.jei.JeiCompat;
import java.util.Collection;
import java.util.OptionalLong;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public final class ItemOrFluidPicker extends ResourceSelectorScreen<Object> {
    private static final SearchModeIndex<ResourceSearchMode<Object>> MODES = new SearchModeIndex<>();

    static {
        if (JeiCompat.isLoaded()) {
            MODES.appendMode(new Mode(
                    ItemIcon.getItemIcon(Items.APPLE),
                    Component.translatable("questaddons.gui.search_mode.jei"),
                    ItemOrFluidPicker::jeiResources,
                    () -> {}));
        }
        MODES.appendMode(wrap(ResourceSearchMode.ALL_ITEMS));
        MODES.appendMode(new Mode(
                ItemIcon.getItemIcon(Items.BUCKET),
                ResourceSearchMode.ALL_FLUIDS.getDisplayName(),
                ResourceSearchMode.ALL_FLUIDS::getAllResources,
                ResourceSearchMode.ALL_FLUIDS::clearCache));
        MODES.appendMode(wrap(ResourceSearchMode.INVENTORY));
    }

    private ItemOrFluidPicker(Config config, ConfigCallback callback) {
        super(config, callback);
    }

    public static Config config() {
        return new Config();
    }

    @Override
    protected SearchModeIndex<ResourceSearchMode<Object>> getSearchModeIndex() {
        return MODES;
    }

    @Override
    protected ResourceButton makeResourceButton(Panel panel, @Nullable SelectableResource<Object> resource) {
        return new PickButton(panel, resource == null ? resource(ItemStack.EMPTY) : resource);
    }

    private static Collection<? extends SelectableResource<?>> jeiResources() {
        return Stream.<SelectableResource<?>>concat(
                        JeiCompat.visibleItems().stream().map(SelectableResource::item),
                        JeiCompat.visibleFluids().stream()
                                .map(fluid -> SelectableResource.fluid(FluidStack.create(
                                        fluid.getFluid(), FluidType.BUCKET_VOLUME, fluid.getComponentsPatch()))))
                .toList();
    }

    private static Mode wrap(ResourceSearchMode<?> mode) {
        return new Mode(mode.getIcon(), mode.getDisplayName(), mode::getAllResources, mode::clearCache);
    }

    private static Resource resource(Object stack) {
        return new Resource(
                stack instanceof FluidStack fluid
                        ? SelectableResource.fluid(fluid)
                        : SelectableResource.item((ItemStack) stack));
    }

    public static RecipeTarget target(Object stack) {
        if (stack instanceof FluidStack fluid) {
            return RecipeTarget.fluid(
                    new net.neoforged.neoforge.fluids.FluidStack(fluid.getFluid(), FluidType.BUCKET_VOLUME));
        }
        return RecipeTarget.item((ItemStack) stack);
    }

    private record Mode(
            Icon icon, Component name, Supplier<Collection<? extends SelectableResource<?>>> resources, Runnable clear)
            implements ResourceSearchMode<Object> {
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public Component getDisplayName() {
            return name;
        }

        @Override
        public Collection<? extends SelectableResource<Object>> getAllResources() {
            return resources.get().stream().map(Resource::new).toList();
        }

        @Override
        public void clearCache() {
            clear.run();
        }
    }

    private record Resource(SelectableResource<?> inner) implements SelectableResource<Object> {
        @Override
        public Object resource() {
            return inner.resource();
        }

        @Override
        public long getCount() {
            return inner.getCount();
        }

        @Override
        public void setCount(int count) {
            inner.setCount(count);
        }

        @Override
        public boolean isEmpty() {
            return inner.isEmpty();
        }

        @Override
        public Component getName() {
            return inner.getName();
        }

        @Override
        public Icon getIcon() {
            return inner.getIcon();
        }

        @Override
        public SelectableResource<Object> copyWithCount(long count) {
            return new Resource(inner.copyWithCount(count));
        }

        @Override
        public CompoundTag getComponentsTag() {
            return inner.getComponentsTag();
        }

        @Override
        public void applyComponentsTag(CompoundTag tag) {
            inner.applyComponentsTag(tag);
        }
    }

    public static final class Config extends ResourceConfigValue<Object> {
        private Config() {
            value = ItemStack.EMPTY;
            defaultValue = ItemStack.EMPTY;
        }

        @Override
        public void onClicked(Widget clicked, MouseButton button, ConfigCallback callback) {
            new ItemOrFluidPicker(this, callback).openGui();
        }

        @Override
        public boolean allowEmptyResource() {
            return false;
        }

        @Override
        public OptionalLong fixedResourceSize() {
            return OptionalLong.of(1);
        }

        @Override
        public boolean isEmpty() {
            return getResource().isEmpty();
        }

        @Override
        public SelectableResource<Object> getResource() {
            return resource(getValue());
        }

        @Override
        public boolean setResource(SelectableResource<Object> resource) {
            return setCurrentValue(resource.resource());
        }
    }

    private final class PickButton extends ResourceButton {
        private PickButton(Panel panel, SelectableResource<Object> resource) {
            super(panel, resource);
        }

        @Override
        public boolean shouldAdd(SearchTerms terms) {
            if (getResource() instanceof FluidStack fluid) {
                return terms.match(
                        BuiltInRegistries.FLUID.getKey(fluid.getFluid()),
                        fluid.getName().getString(),
                        tag -> fluid.getFluid().defaultFluidState().is(TagKey.create(Registries.FLUID, tag)));
            }
            ItemStack stack = (ItemStack) getResource();
            return terms.match(
                    BuiltInRegistries.ITEM.getKey(stack.getItem()),
                    stack.getHoverName().getString(),
                    tag -> stack.is(TagKey.create(Registries.ITEM, tag)));
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (getResource() instanceof FluidStack fluid) {
                list.add(fluid.getName());
                if (FTBLibraryClientConfig.FLUID_MODNAME.get()) {
                    ModUtils.getModName(fluid.getFluid()).ifPresent(name -> list.add(modName(name)));
                }
                return;
            }
            ItemStack stack = (ItemStack) getResource();
            Minecraft mc = Minecraft.getInstance();
            TooltipFlag flag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
            stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, flag)
                    .forEach(list::add);
            if (FTBLibraryClientConfig.ITEM_MODNAME.get()) {
                ModUtils.getModName(stack.getItem()).ifPresent(name -> list.add(modName(name)));
            }
        }

        private static Component modName(String name) {
            return Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
        }
    }
}
