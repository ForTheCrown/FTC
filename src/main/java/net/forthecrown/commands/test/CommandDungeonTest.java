package net.forthecrown.commands.test;

import java.nio.file.Files;
import java.nio.file.Path;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.dungeons.DungeonWorld;
import net.forthecrown.dungeons.level.generator.TreeGenerator;
import net.forthecrown.dungeons.level.generator.TreeGeneratorConfig;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

public class CommandDungeonTest extends FtcCommand {

  private static final Logger LOGGER = Loggers.getLogger();

  public CommandDungeonTest() {
    super("DungeonTest");
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /DungeonTest
   *
   * Permissions used:
   *
   * Main Author:
   */

  static TreeGeneratorConfig getConfig() {
    var path = Path.of("dungeon_generator_config.json");
    TreeGeneratorConfig config;

    if (Files.exists(path)) {
      config = SerializationHelper.readJson(path)
          .resultOrPartial(LOGGER::error)
          .map(TreeGeneratorConfig::deserialize)
          .orElseThrow();
    } else {
      config = TreeGeneratorConfig.defaultConfig();
      SerializationHelper.writeJson(path, config.serialize());
    }

    return config;
  }

  @Override
  protected void createCommand(BrigadierCommand command) {
    command
        .executes(c -> {
          var config = getConfig();

          TreeGenerator.generateAsync(config).whenComplete((level, err) -> {
            if (err != null) {
              c.getSource().sendAdmin("Error generating dungeon, check console");
              return;
            }

            c.getSource().sendMessage("Generated");

            Tasks.runSync(() -> {
              var world = DungeonWorld.get();

              if (world == null) {
                DungeonWorld.reset();
              }

              level.place();
              c.getSource().sendAdmin("Placed dungeon");
            });
          });

          return 0;
        })

        .then(literal("reset_world")
            .executes(c -> {
              DungeonWorld.reset();
              c.getSource().sendAdmin("Reset dungeon world");
              return 0;
            })
        );
  }
}