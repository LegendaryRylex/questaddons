package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import java.util.List;

public interface DescriptionEditor {
    void questaddons$fixJson();

    void questaddons$insert(DescriptionInsert insert, int anchorX, int anchorY);

    boolean questaddons$hasPreview();

    void questaddons$togglePreview();

    List<Widget> questaddons$previewWidgets();
}
