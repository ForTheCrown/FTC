package net.forthecrown.guilds.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildPermissions;

public class GuildInventoryNode extends GuildCommandNode {

  protected GuildInventoryNode() {
    super("guildinventory", "inventory");
    setAliases("ginv", "guildinv");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Opens your guild's inventory");

    factory.usage("<guild>")
        .setPermission(GuildPermissions.GUILD_ADMIN)
        .addInfo("Opens a <guild>'s inventory");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(
      T command
  ) {
    addGuildCommand(command, (c, provider) -> {
      var guild = provider.get(c);
      var player = c.getSource().asPlayer();

      player.openInventory(guild.getInventory());
      return 0;
    });
  }
}