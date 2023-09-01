package net.forthecrown.text.parse;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import net.forthecrown.Permissions;
import net.forthecrown.user.User;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

public enum ChatParseFlag {

  COLORS("color"),
  GRADIENTS("gradients"),

  EMOJIS("emotes"),

  LINKS("links"),
  CLEAN_LINKS("links.clean"),

  IGNORE_SWEARS("ignorebanned"),
  IGNORE_CASE("caseignore"),

  TIMESTAMPS("timestamps"),
  TAGGING("tagging"),
  ;

  private static final ChatParseFlag[] VALUES = values();

  @Getter
  private final Permission permission;

  ChatParseFlag(String permission) {
    this.permission = Permissions.register("ftc.chat." + permission);
  }

  public static Set<ChatParseFlag> allApplicable(Permissible permissible) {
    return allApplicable(permissible::hasPermission);
  }

  public static Set<ChatParseFlag> allApplicable(User user) {
    return allApplicable(user::hasPermission);
  }

  public static Set<ChatParseFlag> allApplicable(Predicate<Permission> predicate) {
    Set<ChatParseFlag> flags = new HashSet<>();

    for (var v: VALUES) {
      if (predicate.test(v.getPermission())) {
        flags.add(v);
      }
    }

    return flags;
  }

  public static Set<ChatParseFlag> all() {
    return EnumSet.allOf(ChatParseFlag.class);
  }
}