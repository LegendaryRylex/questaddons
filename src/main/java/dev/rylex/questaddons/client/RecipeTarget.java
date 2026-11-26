package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.config.gui.resource.FluidStackResource;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public record RecipeTarget(ItemStack stack, FluidStack fluid) {
    public static RecipeTarget item(ItemStack stack) {
        return new RecipeTarget(stack, FluidStack.EMPTY);
    }

    public static RecipeTarget fluid(FluidStack fluid) {
        return new RecipeTarget(ItemStack.EMPTY, fluid);
    }

    public boolean isFluid() {
        return stack.isEmpty();
    }

    public Component name() {
        return isFluid() ? fluid.getHoverName() : stack.getHoverName();
    }

    public String id() {
        return isFluid()
                ? BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString()
                : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public Icon<?> icon() {
        return isFluid()
                ? new FluidStackResource(new dev.ftb.mods.ftblibrary.platform.fluid.FluidStack(
                                fluid.getFluid(), fluid.getAmount()))
                        .getIcon()
                : ItemIcon.ofItemStack(stack);
    }
}
