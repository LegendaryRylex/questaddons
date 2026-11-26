package dev.rylex.questaddons.compat.jei;

import dev.rylex.questaddons.client.RecipeLink;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class JeiRecipeLinks {
    static final String SCHEME = "jei:";

    private static final String RECIPE = "recipe";
    private static final String OUTPUT = "output";
    private static final String INPUT = "input";

    private JeiRecipeLinks() {}

    static List<ItemStack> items() {
        IJeiRuntime runtime = QuestAddonsJeiPlugin.runtime();
        return runtime == null
                ? List.of()
                : List.copyOf(runtime.getIngredientManager().getAllItemStacks());
    }

    static List<RecipeLink> making(ItemStack stack) {
        IJeiRuntime runtime = QuestAddonsJeiPlugin.runtime();
        if (runtime == null) {
            return List.of();
        }
        IRecipeManager recipes = runtime.getRecipeManager();
        IFocus<ItemStack> focus = runtime.getJeiHelpers()
                .getFocusFactory()
                .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack);
        List<RecipeLink> links = new ArrayList<>();
        recipes.createRecipeCategoryLookup()
                .limitFocus(List.of(focus))
                .get()
                .forEach(category -> addLinks(recipes, category, focus, stack, links));
        links.sort(RecipeLink.ORDER);
        return links;
    }

    private static <T> void addLinks(
            IRecipeManager recipes,
            IRecipeCategory<T> category,
            IFocus<ItemStack> focus,
            ItemStack stack,
            List<RecipeLink> links) {
        RecipeType<T> type = category.getRecipeType();
        ItemStack catalyst = recipes.createRecipeCatalystLookup(type)
                .getItemStack()
                .findFirst()
                .orElse(ItemStack.EMPTY);
        String base = SCHEME + type.getUid();
        boolean unnamed = false;
        for (T recipe : recipes.createRecipeLookup(type)
                .limitFocus(List.of(focus))
                .get()
                .toList()) {
            ResourceLocation name = category.getRegistryName(recipe);
            if (name == null) {
                unnamed = true;
            } else {
                links.add(new RecipeLink(
                        base + "?" + RECIPE + "=" + name, category.getTitle(), name.toString(), catalyst));
            }
        }
        if (unnamed) {
            String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            links.add(new RecipeLink(base + "?" + OUTPUT + "=" + item, category.getTitle(), item, catalyst));
        }
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
        IFocus<?> focus = focus(runtime, params);
        if (focus != null) {
            List<T> matching = recipes.createRecipeLookup(type)
                    .limitFocus(List.of(focus))
                    .get()
                    .toList();
            if (!matching.isEmpty()) {
                runtime.getRecipesGui().showRecipes(category, matching, List.of(focus));
                return;
            }
        }
        runtime.getRecipesGui().showTypes(List.of(type));
    }

    private static @Nullable IFocus<?> focus(IJeiRuntime runtime, Map<String, String> params) {
        boolean output = params.containsKey(OUTPUT);
        ResourceLocation itemId = id(output ? params.get(OUTPUT) : params.get(INPUT));
        if (itemId == null) {
            return null;
        }
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
        if (item.isEmpty()) {
            return null;
        }
        RecipeIngredientRole role = output ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;
        return runtime.getJeiHelpers()
                .getFocusFactory()
                .createFocus(role, VanillaTypes.ITEM_STACK, new ItemStack(item.get()));
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
