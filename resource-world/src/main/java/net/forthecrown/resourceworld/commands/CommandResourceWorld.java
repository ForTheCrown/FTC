package net.forthecrown.resourceworld.commands;

import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.resourceworld.RwPlugin;
import net.kyori.adventure.text.Component;

@CommandData("file = 'command.gcn'")
public class CommandResourceWorld {

  private final RwPlugin plugin;

  public CommandResourceWorld(RwPlugin plugin) {
    this.plugin = plugin;
  }

  void reloadPlugin(CommandSource source) {
    plugin.reload();
    source.sendSuccess(Component.text("Reloaded ResourceWorld plugin"));
  }
}
