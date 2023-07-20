package net.forthecrown.command.settings;

import static net.forthecrown.text.Messages.BUTTON_ACCEPT_TICK;
import static net.forthecrown.text.Messages.BUTTON_DENY_CROSS;

import com.google.common.base.Strings;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter(AccessLevel.PACKAGE)
public abstract class BookSetting<C> {

  private SettingsBook<C> book;

  public abstract Component displayName();

  public abstract Component createButtons(C context);

  public abstract boolean shouldInclude(C context);

  public static Component createButton(
      boolean toggle,
      boolean current,
      String cmd,
      @NotNull HoverEventSource hover
  ) {
    return createButton(
        toggle,
        current,
        Strings.isNullOrEmpty(cmd)
            ? null
            : ClickEvent.runCommand(cmd),
        hover
    );
  }

  public static Component createButton(
      boolean toggle,
      boolean current,
      ClickEvent event,
      @NotNull HoverEventSource hover
  ) {
    Objects.requireNonNull(hover);

    var builder = (toggle ? BUTTON_ACCEPT_TICK : BUTTON_DENY_CROSS).toBuilder();

    if (current == toggle) {
      builder.color(NamedTextColor.DARK_AQUA).hoverEvent(null);
    } else {
      builder.color(NamedTextColor.GRAY).hoverEvent(hover);
    }

    return builder.clickEvent(event).build();
  }
}