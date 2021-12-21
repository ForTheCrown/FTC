package net.forthecrown.core.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

public interface ComponentPrinter extends ComponentLike {
    Component print();
    Component printCurrent();

    @Override
    @NotNull
    default Component asComponent() {
        return print();
    }
}
