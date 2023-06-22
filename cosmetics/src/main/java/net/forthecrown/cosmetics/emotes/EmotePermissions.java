package net.forthecrown.cosmetics.emotes;

import static net.forthecrown.Permissions.register;
import static net.forthecrown.Permissions.registerPrefixed;

import org.bukkit.permissions.Permission;

public interface EmotePermissions {

  Permission EMOTES                  = register("ftc.emotes");
  Permission EMOTE_IGNORE            = registerPrefixed(EMOTES, "cooldown.ignore");
  Permission EMOTE_JINGLE            = registerPrefixed(EMOTES, "jingle");
  Permission EMOTE_POG               = registerPrefixed(EMOTES, "pog");
  Permission EMOTE_SCARE             = registerPrefixed(EMOTES, "scare");
  Permission EMOTE_HUG               = registerPrefixed(EMOTES, "hug");

}