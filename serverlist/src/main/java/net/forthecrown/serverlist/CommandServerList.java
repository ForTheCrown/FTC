package net.forthecrown.serverlist;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandServerList extends FtcCommand {

  public CommandServerList() {
    super("serverlist");
    setDescription("Allows interacting with the serverlist plugin");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("reload")
            .executes(c -> {
              ServerlistPlugin plugin = JavaPlugin.getPlugin(ServerlistPlugin.class);
              plugin.reload();

              c.getSource().sendSuccess(Component.text("Reloaded serverlist plugin"));
              return 0;
            })
        );
  }
}
