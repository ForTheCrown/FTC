package net.forthecrown.commands.test;

import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.utils.Particles;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.Vectors;
import org.apache.logging.log4j.Logger;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.spongepowered.math.vector.Vector3d;

public class CommandBoundsDraw extends FtcCommand {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final List<BoundingBox> BOXES = new ObjectArrayList<>();

  private static BukkitTask task;

  public static final Color[] COLORS = {
      Color.RED,
      Color.GREEN,
      Color.BLACK,
      Color.BLUE,
      Color.PURPLE,
      Color.ORANGE,
      Color.SILVER,
      Color.WHITE,
      Color.YELLOW,
  };

  public CommandBoundsDraw() {
    super("BoundsDraw");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /BoundsDraw
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          if (task == null) {
            task = Tasks.runTimer(() -> {
              for (int i = 0; i < BOXES.size(); i++) {
                BoundingBox b = BOXES.get(i);

                try {
                  Vector3d min =
                      Vectors.doubleFrom(b.getMin());

                  Vector3d max =
                      Vectors.doubleFrom(b.getMax());

                  Color color = COLORS[i % BOXES.size()];

                  Particles.drawBounds(
                      min, max, Worlds.overworld(),
                      color
                  );
                } catch (Throwable t) {
                  LOGGER.error("Couldn't draw bounds {}",
                      b, t
                  );
                }
              }

            }, 15, 15);

            c.getSource().sendMessage("Now drawing");
            return 0;
          }

          Tasks.cancel(task);

          c.getSource().sendMessage("No longer drawing");
          return 0;
        })

        .then(literal("clear")
            .executes(c -> {
              BOXES.clear();

              c.getSource().sendMessage("Cleared");
              return 0;
            })
        )

        .then(literal("remove")
            .executes(c -> {
              if (BOXES.isEmpty()) {
                c.getSource().sendMessage("EMPTY");
                return 0;
              }

              BOXES.remove(BOXES.size() - 1);

              c.getSource().sendMessage("Removed last");
              return 0;
            })
        )

        .then(literal("add")
            .then(argument("min", ArgumentTypes.position())
                .then(argument("max", ArgumentTypes.position())
                    .executes(this::add)
                )
            )
        );
  }

  private int add(CommandContext<CommandSource> c) {
    Location min = ArgumentTypes.getLocation(c, "min");
    Location max = ArgumentTypes.getLocation(c, "max");

    BoundingBox box = BoundingBox.of(min, max);
    BOXES.add(box);

    c.getSource().sendMessage("Created box");
    return 0;
  }
}