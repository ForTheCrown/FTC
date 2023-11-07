package net.forthecrown.cosmetics.emotes;

import static net.forthecrown.Permissions.register;

import org.bukkit.permissions.Permission;

public interface EmotePermissions {

  Permission EMOTES                  = register("ftc.emotes");
  Permission EMOTES_ADMIN            = register(EMOTES, "admin");
  Permission EMOTE_IGNORE            = register(EMOTES, "cooldown.ignore");
  Permission EMOTE_JINGLE            = register(EMOTES, "jingle");
  Permission EMOTE_POG               = register(EMOTES, "pog");
  Permission EMOTE_SCARE             = register(EMOTES, "scare");
  Permission EMOTE_HUG               = register(EMOTES, "hug");

}