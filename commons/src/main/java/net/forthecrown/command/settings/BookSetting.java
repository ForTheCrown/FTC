package net.forthecrown.command.settings;

import static net.forthecrown.text.Messages.BUTTON_ACCEPT_TICK;
import static net.forthecrown.text.Messages.BUTTON_DENY_CROSS;

import com.google.common.base.Strings;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public interface BookSetting<C> {

  Component displayName();

  Component createButtons(C context);

  boolean shouldInclude(C context);

  static Component createButton(
      boolean toggle,
      boolean current,
      String cmd,
      @NotNull HoverEventSource hover
  ) {
    Objects.requireNonNull(hover);

    var builder = (toggle ? BUTTON_ACCEPT_TICK : BUTTON_DENY_CROSS).toBuilder();

    if (current == toggle) {
      builder.color(NamedTextColor.DARK_AQUA).hoverEvent(null);
    } else {
      builder.color(NamedTextColor.GRAY).hoverEvent(hover);
    }

    return builder
        .clickEvent(Strings.isNullOrEmpty(cmd) ? null : ClickEvent.runCommand(cmd))
        .build();
  }
}