package dev.rylex.questaddons.client;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public final class FluidLink {
    private static final String PREFIX = "questaddons:fluid/";

    private FluidLink() {}

    public static Style apply(Style style, FluidStack fluid) {
        return style.withInsertion(PREFIX + BuiltInRegistries.FLUID.getKey(fluid.getFluid()))
                .withHoverEvent(new HoverEvent.ShowText(fluid.getHoverName()));
    }

    public static Optional<FluidStack> fluid(Style style) {
        String insertion = style == null ? null : style.getInsertion();
        if (insertion == null || !insertion.startsWith(PREFIX)) {
            return Optional.empty();
        }
        Identifier id = Identifier.tryParse(insertion.substring(PREFIX.length()));
        Fluid fluid = id == null
                ? Fluids.EMPTY
                : BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY);
        return fluid == Fluids.EMPTY ? Optional.empty() : Optional.of(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }
}
