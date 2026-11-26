package dev.rylex.questaddons.compat.jei;

import dev.rylex.questaddons.client.RecipeLink;
import dev.rylex.questaddons.client.RecipeTarget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

final class JeiRecipeLinks {
    static final String SCHEME = "jei:";

    private static final String RECIPE = "recipe";
    private static final String OUTPUT = "output";
    private static final String INPUT = "input";
    private static final String OUTPUT_FLUID = "output_fluid";
    private static final String INPUT_FLUID = "input_fluid";

    private JeiRecipeLinks() {}

    static List<RecipeTarget> targets() {
        IJeiRuntime runtime = QuestAddonsJeiPlugin.runtime();
        if (runtime == null) {
            return List.of();
        }
        IIngredientManager ingredients = runtime.getIngredientManager();
        return Stream.concat(
                        ingredients.getAllItemStacks().stream().map(RecipeTarget::item),
                        ingredients.getAllIngredients(NeoForgeTypes.FLUID_STACK).stream()
                                .map(RecipeTarget::fluid))
                .toList();
    }

    static List<RecipeLink> making(RecipeTarget target) {
        IJeiRuntime runtime = QuestAddonsJeiPlugin.runtime();
        if (runtime == null) {
            return List.of();
        }
        IRecipeManager recipes = runtime.getRecipeManager();
        List<IFocus<?>> focuses = focuses(runtime, RecipeIngredientRole.OUTPUT, target);
        List<RecipeLink> links = new ArrayList<>();
        recipes.createRecipeCategoryLookup()
                .limitFocus(focuses)
                .get()
                .forEach(category -> addLinks(recipes, category, focuses, target, links));
        links.sort(RecipeLink.ORDER);
        return links;
    }

    private static <T> void addLinks(
            IRecipeManager recipes,
            IRecipeCategory<T> category,
            List<IFocus<?>> focuses,
            RecipeTarget target,
            List<RecipeLink> links) {
        RecipeType<T> type = category.getRecipeType();
        ItemStack catalyst = recipes.createRecipeCatalystLookup(type)
                .getItemStack()
                .findFirst()
                .orElse(ItemStack.EMPTY);
        String base = SCHEME + type.getUid();
        boolean unnamed = false;
        for (T recipe :
                recipes.createRecipeLookup(type).limitFocus(focuses).get().toList()) {
            ResourceLocation name = category.getRegistryName(recipe);
            if (name == null) {
                unnamed = true;
            } else {
                links.add(new RecipeLink(
                        base + "?" + RECIPE + "=" + name, category.getTitle(), name.toString(), catalyst));
            }
        }
        if (unnamed) {
            String key = target.isFluid() ? OUTPUT_FLUID : OUTPUT;
            links.add(new RecipeLink(base + "?" + key + "=" + target.id(), category.getTitle(), target.id(), catalyst));
        }
    }

    private static List<IFocus<?>> focuses(IJeiRuntime runtime, RecipeIngredientRole role, RecipeTarget target) {
        IFocusFactory factory = runtime.getJeiHelpers().getFocusFactory();
        List<IFocus<?>> focuses = new ArrayList<>();
        FluidStack fluid = target.fluid();
        if (!target.isFluid()) {
            focuses.add(factory.createFocus(role, VanillaTypes.ITEM_STACK, target.stack()));
            fluid = FluidUtil.getFluidContained(target.stack()).orElse(FluidStack.EMPTY);
        }
        if (!fluid.isEmpty()) {
            focuses.add(factory.createFocus(role, NeoForgeTypes.FLUID_STACK, fluid));
        }
        return focuses;
    }

    static boolean open(String value) {
        IJeiRuntime runtime = QuestAddonsJeiPlugin.runtime();
        if (runtime == null || !value.startsWith(SCHEME)) {
            return false;
        }
        String body = value.substring(SCHEME.length());
        int query = body.indexOf('?');
        ResourceLocation uid = ResourceLocation.tryParse(query < 0 ? body : body.substring(0, query));
        if (uid == null) {
            return false;
        }
        Map<String, String> params = query < 0 ? Map.of() : params(body.substring(query + 1));
        Optional<RecipeType<?>> type = runtime.getJeiHelpers().getRecipeType(uid);
        if (type.isEmpty()) {
            return false;
        }
        show(runtime, type.get(), params);
        return true;
    }

    private static <T> void show(IJeiRuntime runtime, RecipeType<T> type, Map<String, String> params) {
        IRecipeManager recipes = runtime.getRecipeManager();
        IRecipeCategory<T> category = recipes.getRecipeCategory(type);
        ResourceLocation recipeId = id(params.get(RECIPE));
        if (recipeId != null) {
            List<T> matching = recipes.createRecipeLookup(type)
                    .get()
                    .filter(recipe -> recipeId.equals(category.getRegistryName(recipe)))
                    .limit(1)
                    .toList();
            if (!matching.isEmpty()) {
                runtime.getRecipesGui().showRecipes(category, matching, List.of());
                return;
            }
        }
        List<IFocus<?>> focuses = focuses(runtime, params);
        if (!focuses.isEmpty()) {
            List<T> matching =
                    recipes.createRecipeLookup(type).limitFocus(focuses).get().toList();
            if (!matching.isEmpty()) {
                runtime.getRecipesGui().showRecipes(category, matching, focuses);
                return;
            }
        }
        runtime.getRecipesGui().showTypes(List.of(type));
    }

    private static List<IFocus<?>> focuses(IJeiRuntime runtime, Map<String, String> params) {
        boolean output = params.containsKey(OUTPUT) || params.containsKey(OUTPUT_FLUID);
        RecipeIngredientRole role = output ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;
        RecipeTarget target = target(params, output ? OUTPUT : INPUT, output ? OUTPUT_FLUID : INPUT_FLUID);
        return target == null ? List.of() : focuses(runtime, role, target);
    }

    private static @Nullable RecipeTarget target(Map<String, String> params, String itemKey, String fluidKey) {
        ResourceLocation itemId = id(params.get(itemKey));
        if (itemId != null) {
            return BuiltInRegistries.ITEM
                    .getOptional(itemId)
                    .map(item -> RecipeTarget.item(new ItemStack(item)))
                    .orElse(null);
        }
        ResourceLocation fluidId = id(params.get(fluidKey));
        if (fluidId == null) {
            return null;
        }
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(fluidId).orElse(Fluids.EMPTY);
        return fluid == Fluids.EMPTY ? null : RecipeTarget.fluid(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    private static Map<String, String> params(String query) {
        return Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(pair -> pair.length == 2)
                .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1], (first, second) -> first));
    }

    private static @Nullable ResourceLocation id(@Nullable String value) {
        return value == null ? null : ResourceLocation.tryParse(value);
    }
}
