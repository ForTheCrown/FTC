package net.forthecrown.antigrief;

import static net.forthecrown.Permissions.register;

import org.bukkit.permissions.Permission;

public interface GriefPermissions {

  String PUNISH_PREFIX = "ftc.punish.";

  Permission STAFF_CHAT              = register("ftc.staffchat");
  Permission PUNISH_MUTE             = register(PUNISH_PREFIX + "mute");
  Permission PUNISH_SOFTMUTE         = register(PUNISH_PREFIX + "softmute");
  Permission PUNISH_JAIL             = register(PUNISH_PREFIX + "jail");
  Permission PUNISH_KICK             = register(PUNISH_PREFIX + "kick");
  Permission PUNISH_BAN              = register(PUNISH_PREFIX + "ban");
  Permission PUNISH_BANIP            = register(PUNISH_PREFIX + "banip");
  Permission PUNISH_NOTES            = register(PUNISH_PREFIX + "notes");
  Permission PUNISH_SEPARATE         = register(PUNISH_PREFIX + "separate");
  Permission EAVESDROP               = register("ftc.eavesdrop");
  Permission EAVESDROP_ADMIN         = register(EAVESDROP.getName() + ".admin");
  Permission VANISH                  = register("ftc.vanish");
  Permission VANISH_SEE              = register(VANISH.getName() + ".see");
}