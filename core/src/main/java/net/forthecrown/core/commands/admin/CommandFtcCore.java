package net.forthecrown.core.commands.admin;

import net.forthecrown.core.CorePlugin;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.CommandFile;
import net.kyori.adventure.text.Component;

@CommandFile("commands/ftccore.gcn")
public class CommandFtcCore {

  void reload(CommandSource source) {
    CorePlugin plugin = CorePlugin.plugin();
    plugin.reload();

    source.sendSuccess(Component.text("Reloaded FTC plugin"));
  }

  void reloadConfig(CommandSource source) {
    CorePlugin plugin = CorePlugin.plugin();
    plugin.reloadConfig();

    source.sendSuccess(Component.text("Reloaded FTC config"));
  }

  void save(CommandSource source) {
    CorePlugin plugin = CorePlugin.plugin();
    plugin.save();

    source.sendSuccess(Component.text("Saved FTC plugin"));
  }
}
