package dev.rylex.questaddons.compat.jei;

import dev.rylex.questaddons.QuestAddons;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public final class QuestAddonsJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(QuestAddons.MOD_ID, "jei_plugin");

    @Nullable
    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGlobalGuiHandler(new TrackerGuiHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    static @Nullable IJeiRuntime runtime() {
        return runtime;
    }

    static boolean isJeiScreen(Screen screen) {
        return runtime != null
                && runtime.getScreenHelper().getGuiProperties(screen).isPresent();
    }

    static boolean hasKeyboardFocus() {
        return runtime != null && runtime.getIngredientListOverlay().hasKeyboardFocus();
    }

    static void showRecipes(ItemStack stack, boolean uses) {
        if (runtime == null) {
            return;
        }
        IFocusFactory focuses = runtime.getJeiHelpers().getFocusFactory();
        RecipeIngredientRole role = uses ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
        runtime.getRecipesGui().show(focuses.createFocus(role, VanillaTypes.ITEM_STACK, stack));
    }
}
