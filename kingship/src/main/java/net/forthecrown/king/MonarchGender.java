package net.forthecrown.king;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

import lombok.Getter;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;

@Getter
public enum MonarchGender {

  KING,
  QUEEN,
  MONARCH;

  private final String title;

  private final Component prefix;

  MonarchGender() {
    this.title = Text.prettyEnumName(this);
    this.prefix = makeTitle(title);
  }

  private static Component makeTitle(String name) {
    return Component.textOfChildren(
        text("[", WHITE, BOLD),
        text(name, YELLOW, BOLD),
        text("] ", WHITE, BOLD)
    );
  }
}
