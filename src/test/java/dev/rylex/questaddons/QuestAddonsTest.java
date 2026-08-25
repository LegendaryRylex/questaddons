package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QuestAddonsTest {
    @Test
    void modIdIsValid() {
        assertTrue(QuestAddons.MOD_ID.matches("[a-z][a-z0-9_]{1,63}"));
    }
}
