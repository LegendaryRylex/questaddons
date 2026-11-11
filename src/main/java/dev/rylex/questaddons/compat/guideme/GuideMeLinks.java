package dev.rylex.questaddons.compat.guideme;

import dev.rylex.questaddons.client.DocsLink;
import guideme.Guide;
import guideme.Guides;
import guideme.compiler.FrontmatterNavigation;
import guideme.compiler.ParsedGuidePage;
import java.util.ArrayList;
import java.util.List;

final class GuideMeLinks {
    private GuideMeLinks() {}

    static List<DocsLink> all() {
        List<DocsLink> links = new ArrayList<>();
        for (Guide guide : Guides.getAll()) {
            for (ParsedGuidePage page : guide.getPages()) {
                links.add(new DocsLink(
                        GuideMeCompat.MOD_ID + "," + guide.getId() + "," + page.getId(),
                        title(page),
                        guide.getId().toString()));
            }
        }
        links.sort(DocsLink.ORDER);
        return links;
    }

    private static String title(ParsedGuidePage page) {
        FrontmatterNavigation navigation =
                page.getFrontmatter() == null ? null : page.getFrontmatter().navigationEntry();
        return navigation == null || navigation.title() == null ? page.getId().getPath() : navigation.title();
    }
}
