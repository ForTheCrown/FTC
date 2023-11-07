package net.forthecrown.core.commands.admin;

import java.lang.management.ManagementFactory;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class CommandMemory extends FtcCommand {

  static final int BYTES_PER_MB = (int) Math.pow(1024, 2);

  public CommandMemory() {
    super("memory");

    setAliases("mem");
    setPermission(CorePermissions.CMD_MEMORY);
    setDescription("Displays current memory usage information");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {

      var worlds = TextJoiner.onNewLine()
          .add(Bukkit.getWorlds().stream()
              .map(world -> {
                return Text.format(
                    "&6{0}: "
                        + "&eEntities:&7 {1}, "
                        + "&eLoaded Chunks:&7 {2}, "
                        + "&eTile entities:&7 {3}, "
                        + "&eTicking tile entities:&7 {4}",

                    world.getName(),
                    world.getEntityCount(),
                    world.getChunkCount(),
                    world.getTileEntityCount(),
                    world.getTickableTileEntityCount()
                );
              })
          )
          .asComponent();

      Runtime runtime = Runtime.getRuntime();
      long maxMem = runtime.maxMemory();
      long totalMem = runtime.totalMemory();
      long freeMem = runtime.freeMemory();

      c.getSource().sendMessage(
          Text.format(
              """
              &eUptime:&7 {0, time, -timestamp}
              &eTPS:&7 {1}
              &eMax memory:&7 {2} Mib
              &eTotal memory:&7 {3} Mib
              &eFree memory:&7 {4} Mib
              &eAverage tick time:&7 {5, time}
              &eWorlds: \n&7{6}
              """,

              ManagementFactory.getRuntimeMXBean().getStartTime(),
              getTPS(),
              maxMem / BYTES_PER_MB,
              totalMem / BYTES_PER_MB,
              freeMem / BYTES_PER_MB,
              Bukkit.getAverageTickTime(),
              worlds
          )
      );
      return 0;
    });
  }

  private Component getTPS() {
    var tps = Bukkit.getTPS();

    return Text.format(
        "{0, number, -floor}: 1m, {1, number, -floor}: 5m, {2, number, -floor}: 15m",
        tps[0], tps[1], tps[2]
    );
  }
}