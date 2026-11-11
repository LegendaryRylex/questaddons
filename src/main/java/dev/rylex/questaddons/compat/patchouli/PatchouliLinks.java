package dev.rylex.questaddons.compat.patchouli;

import dev.rylex.questaddons.client.DocsLink;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import vazkii.patchouli.client.book.BookContents;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

final class PatchouliLinks {
    private PatchouliLinks() {}

    static List<DocsLink> all() {
        List<DocsLink> links = new ArrayList<>();
        for (Book book : BookRegistry.INSTANCE.books.values()) {
            BookContents contents = book.getContents();
            if (contents == null || contents.isErrored()) {
                continue;
            }
            String bookName = I18n.get(book.name);
            for (BookEntry entry : contents.entries.values()) {
                links.add(new DocsLink(
                        PatchouliCompat.MOD_ID + "," + book.id + "," + entry.getId(),
                        entry.getName().getString(),
                        bookName));
            }
        }
        links.sort(DocsLink.ORDER);
        return links;
    }
}
