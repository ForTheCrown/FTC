package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.grenadier.CommandSource;

public class GuildInventoryNode extends GuildCommandNode {

  protected GuildInventoryNode() {
    super("guildinventory", "inventory");
    setAliases("ginv", "guildinv");
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