package net.forthecrown.commands.admin;

import java.lang.management.ManagementFactory;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class CommandMemory extends FtcCommand {

  public CommandMemory() {
    super("memory");

    setAliases("mem");
    setPermission(Permissions.CMD_MEMORY);
    setDescription("Displays current memory usage information");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          var worlds = TextJoiner.onNewLine()
              .add(Bukkit.getWorlds().stream()
                  .map(world -> {
                    return Text.format(
                        "&e{0}: " +
                            "&eEntities:&7 {1}, " +
                            "&eLoaded Chunks:&7 {2}, " +
                            "&eTile entities:&7 {3}, " +
                            "&eTicking tile entities:&7 {4}",

                        world.getName(),
                        world.getEntityCount(),
                        world.getLoadedChunks().length,
                        world.getTileEntityCount(),
                        world.getTickableTileEntityCount()
                    );
                  })
              )
              .asComponent();

          c.getSource().sendMessage(
              Text.format(
                  """
                      &eUptime:&7 {0, time, -timestamp}
                      &eTPS:&7 {1}
                      &eMax memory:&7 {2} Mb
                      &eFree memory:&7 {3} Mb
                      &eWorlds: &7{4}
                      """,

                  ManagementFactory.getRuntimeMXBean().getStartTime(),
                  getTPS(),
                  Runtime.getRuntime().maxMemory() / 1_000_000,
                  Runtime.getRuntime().freeMemory() / 1_000_000,
                  worlds
              )
          );
          return 0;
        });
  }

  private Component getTPS() {
    var tps = Bukkit.getTPS();

    return Text.format("{0, number}: 1m, {1, number}: 5m, {2, number}: 15m",
        tps[0], tps[1], tps[2]
    );
  }
}