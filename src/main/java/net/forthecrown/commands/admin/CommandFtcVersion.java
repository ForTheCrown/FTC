package net.forthecrown.commands.admin;

import java.io.InputStreamReader;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.utils.text.Text;
import org.bukkit.configuration.file.YamlConfiguration;

public class CommandFtcVersion extends FtcCommand {

  public CommandFtcVersion() {
    super("FtcVersion");

    setPermission(Permissions.ADMIN);
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      var pluginInput = FTC.getPlugin().getResource("plugin.yml");
      var yaml = YamlConfiguration
          .loadConfiguration(new InputStreamReader(pluginInput));

      String buildTime = yaml.getString("buildTime");
      String buildId = yaml.getString("buildID");
      boolean debug = yaml.getBoolean("debug_build");

      c.getSource().sendMessage(
          Text.format("build_time={0}, build_id={1}, debug_build={2}",
              buildTime, buildId, debug
          )
      );
      return 0;
    });
  }
}