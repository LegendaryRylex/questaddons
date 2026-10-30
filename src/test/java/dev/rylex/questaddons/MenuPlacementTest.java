package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rylex.questaddons.client.MenuPlacement;
import org.junit.jupiter.api.Test;

class MenuPlacementTest {
    @Test
    void opensAfterTheAnchorWhenThereIsRoom() {
        assertEquals(200, MenuPlacement.beside(100, 200, 80, 0, 480));
    }

    @Test
    void flipsBeforeTheAnchorWhenItWouldOverflow() {
        assertEquals(280, MenuPlacement.beside(360, 460, 80, 0, 480));
    }

    @Test
    void staysInsideWhenNeitherSideFits() {
        assertEquals(0, MenuPlacement.beside(40, 440, 80, 0, 480));
    }

    @Test
    void respectsAnOffsetLimit() {
        assertEquals(240, MenuPlacement.beside(250, 340, 80, 240, 400));
    }

    @Test
    void slidesUpWhenItWouldRunPastTheBottom() {
        assertEquals(170, MenuPlacement.within(230, 100, 0, 270));
    }

    @Test
    void keepsItsPositionWhenItFits() {
        assertEquals(40, MenuPlacement.within(40, 100, 0, 270));
    }

    @Test
    void pinsToTheStartWhenTallerThanTheLimit() {
        assertEquals(0, MenuPlacement.within(40, 300, 0, 270));
    }
}
