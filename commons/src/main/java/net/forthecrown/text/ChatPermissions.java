package net.forthecrown.text;

import static net.forthecrown.Permissions.register;

import org.bukkit.permissions.Permission;

public final  class ChatPermissions {
  private ChatPermissions() {}

  public static final Permission CHAT_IGNORE_CASE     = register("ftc.chat.caseignore");
  public static final Permission IGNORE_SWEARS        = register("ftc.chat.ignorebanned");
  public static final Permission CHAT_EMOTES          = register("ftc.chat.emotes");
  public static final Permission CHAT_COLORS          = register("ftc.chat.color");
  public static final Permission CHAT_LINKS           = register("ftc.chat.links");
  public static final Permission CHAT_PLAYER_TAGGING  = register("ftc.chat.tagging");
  public static final Permission CHAT_TIMESTAMPS      = register("ftc.chat.timestamps");
  public static final Permission CHAT_CLEAN_LINKS     = register("ftc.chat.links.clean");
  public static final Permission CHAT_GRADIENTS       = register("ftc.chat.gradients");
}