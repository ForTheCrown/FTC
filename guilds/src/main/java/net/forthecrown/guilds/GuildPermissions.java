package net.forthecrown.guilds;

import static net.forthecrown.Permissions.register;

import org.bukkit.permissions.Permission;

public interface GuildPermissions {

  Permission GUILD                   = register("ftc.guild");
  Permission GUILD_ADMIN             = register(GUILD, "admin");
}
