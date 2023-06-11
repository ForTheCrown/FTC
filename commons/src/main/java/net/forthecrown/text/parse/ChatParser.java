package net.forthecrown.text.parse;

import java.util.EnumSet;
import lombok.Getter;
import net.forthecrown.Permissions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.Permission;

public interface ChatParser {

  EnumSet<ParseFlag> flags();

  Component parse(String input);

  interface Builder {

    Builder addFlag(ParseFlag flag);

    Builder removeFlag(ParseFlag flag);

    Builder setViewer(Audience viewer);

    ChatParser build();
  }

  enum ParseFlag {

    COLORS        ("color"),
    GRADIENTS     ("gradients"),

    EMOJIS        ("emotes"),

    LINKS         ("links"),
    CLEAN_LINKS   ("links.clean"),

    IGNORE_SWEARS ("ignorebanned"),
    IGNORE_CASE   ("caseignore"),

    TIMESTAMPS    ("timestamps"),
    TAGGING       ("tagging"),
    ;

    @Getter
    private final Permission permission;

    ParseFlag(String permission) {
      this.permission = Permissions.register("ftc.chat." + permission);
    }
  }
}