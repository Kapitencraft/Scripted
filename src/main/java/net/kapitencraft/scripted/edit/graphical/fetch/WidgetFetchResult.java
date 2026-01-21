package net.kapitencraft.scripted.edit.graphical.fetch;

import net.kapitencraft.scripted.edit.graphical.widgets.CodeWidget;
import org.jetbrains.annotations.NotNull;

public interface WidgetFetchResult {

    boolean removed();

    int x();

    int y();

    WidgetFetchResult setRemoved();

    @NotNull CodeWidget widget();
}
