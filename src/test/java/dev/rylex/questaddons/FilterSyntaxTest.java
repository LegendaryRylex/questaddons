package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rylex.questaddons.compat.ftbfiltersystem.FilterSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilterSyntaxTest {

    @Test
    void orJoinsItemTermsWithNoSeparators() {
        assertEquals(
                "or(item(ltxi:rocket_turret)item(ltxi:arc_turret))",
                FilterSyntax.or(
                        List.of(FilterSyntax.item("ltxi:rocket_turret"), FilterSyntax.item("ltxi:arc_turret"))));
    }

    @Test
    void orKeepsASingleItemWrapped() {
        assertEquals("or(item(minecraft:stone))", FilterSyntax.or(List.of(FilterSyntax.item("minecraft:stone"))));
    }

    @Test
    void orEmbedsAnExistingFilterVerbatim() {
        assertEquals(
                "or(ftbfiltersystem:item_tag(c:ingots)item(minecraft:stone))",
                FilterSyntax.or(List.of("ftbfiltersystem:item_tag(c:ingots)", FilterSyntax.item("minecraft:stone"))));
    }
}
