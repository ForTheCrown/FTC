package net.forthecrown.cosmetics.emotes;

import static net.forthecrown.Permissions.register;
import static net.forthecrown.Permissions.register;

import net.forthecrown.Permissions;
import org.bukkit.permissions.Permission;

public interface EmotePermissions {

  Permission EMOTES                  = register("ftc.emotes");
  Permission EMOTE_IGNORE            = Permissions.register(EMOTES, "cooldown.ignore");
  Permission EMOTE_JINGLE            = Permissions.register(EMOTES, "jingle");
  Permission EMOTE_POG               = Permissions.register(EMOTES, "pog");
  Permission EMOTE_SCARE             = Permissions.register(EMOTES, "scare");
  Permission EMOTE_HUG               = Permissions.register(EMOTES, "hug");

}