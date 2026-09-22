package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ReadmeKeybindsTest {
    private static final Pattern KEYBIND_ENTRY =
            Pattern.compile("\"key\\.questaddons\\.[a-z_]+\"\\s*:\\s*\"([^\"(]+?)\\s*(?:\\(|\")");

    @Test
    void readmeKeybindTableMatchesRegisteredKeybinds() throws IOException {
        Path root = projectRoot();
        String lang = Files.readString(root.resolve("src/main/resources/assets/questaddons/lang/en_us.json"));
        Set<String> registered = new TreeSet<>();
        Matcher matcher = KEYBIND_ENTRY.matcher(lang);
        while (matcher.find()) {
            registered.add(matcher.group(1).trim());
        }

        Set<String> documented = new TreeSet<>();
        boolean inSection = false;
        for (String line : Files.readAllLines(root.resolve("README.md"))) {
            if (line.startsWith("## ")) {
                inSection = line.equals("## Keybinds");
                continue;
            }
            if (!inSection || !line.startsWith("|") || line.startsWith("|---")) {
                continue;
            }
            String name = line.substring(1, line.indexOf('|', 1)).trim();
            if (!name.equals("Keybind")) {
                documented.add(name);
            }
        }

        assertEquals(
                registered,
                documented,
                "README.md's Keybinds table must list exactly the keybinds in en_us.json, by their display name");
    }

    private static Path projectRoot() {
        for (Path candidate = Paths.get("").toAbsolutePath(); candidate != null; candidate = candidate.getParent()) {
            if (Files.isRegularFile(candidate.resolve("README.md"))
                    && Files.isDirectory(candidate.resolve(Paths.get("src", "main", "java")))) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Could not locate the project root from " + Paths.get("").toAbsolutePath());
    }
}
