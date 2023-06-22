package net.forthecrown.discord;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

class AppenderCommand extends FtcCommand {

  public AppenderCommand() {
    super("discord_appender_reload");
    setDescription("Reloads the console->discord appender");
    simpleUsages();
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(context -> {
      DiscordPlugin plugin = JavaPlugin.getPlugin(DiscordPlugin.class);
      plugin.reloadConfig();

      context.getSource().sendSuccess(
          Component.text("Reloaded console->discord appender")
      );
      return 0;
    });
  }
}