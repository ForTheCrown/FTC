package net.forthecrown.core.commands.admin;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.ExpandedEntityArgument;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.grenadier.types.ParsedPosition.Type;
import net.forthecrown.text.Text;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class CommandLaunch extends FtcCommand {

  public CommandLaunch() {
    super("launch");

    setPermission(CorePermissions.CMD_LAUNCH);
    setAliases("rocket");
    setDescription("Rockets a player in a given direction");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Launches entities with the given vector :3
   *
   * Valid usages of command:
   * /launch <entities> <vector>
   * /rocket <entities> <vector>
   *
   * Permissions used: ftc.admin
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    var prefix = factory.withPrefix("<entities>");

    prefix.usage("")
        .addInfo("Launches every selected entity in")
        .addInfo("the direction you're looking");

    prefix.usage("<velocity: x,y,z>")
        .addInfo("Sets the velocity of every entity");

    prefix.usage("add <velocity: x,y,z>")
        .addInfo("Adds to the velocity of every entity");

    prefix.usage("at <position: x,y,z> [<multiplier: number>]")
        .addInfo("Launches all <entities> towards a specified <position>")
        .addInfo("<multiplier> is an optional scalar for the velocity");

    prefix.usage("at entity <selector> [<multiplier: number>]")
        .addInfo("Launches all entities at the selected entities")
        .addInfo("<multiplier> is an optional scalar for the velocity");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("entity", new ExpandedEntityArgument(true, false))
            .executes(c -> {
              Player player = c.getSource().asPlayer();
              Vector dir = player.getLocation().getDirection();

              return launch(c, dir, false);
            })

            .then(argument("vec", ArgumentTypes.position())
                .executes(c -> launchVelocityGiven(c, false))
            )

            .then(literal("add")
                .then(argument("vec", ArgumentTypes.position())
                    .executes(c -> launchVelocityGiven(c, false))
                )
            )

            .then(literal("at")
                .then(literal("entity")
                    .then(argument("selector", ArgumentTypes.entity())
                        .executes(c -> {
                          var target = ArgumentTypes.getEntity(c, "selector");
                          return launchAt(c, target::getLocation, 1);
                        })

                        .then(argument("scalar", DoubleArgumentType.doubleArg())
                            .executes(c -> {
                              var target = ArgumentTypes.getEntity(c, "selector");
                              double scalar = c.getArgument("scalar", Double.class);
                              return launchAt(c, target::getLocation, scalar);
                            })
                        )
                    )
                )

                .then(argument("position", ArgumentTypes.position())
                    .executes(c -> {
                      var pos = c.getArgument("position", ParsedPosition.class);
                      return launchAt(c, () -> pos.apply(c.getSource()), 1);
                    })

                    .then(argument("scalar", DoubleArgumentType.doubleArg())
                        .executes(c -> {
                          var pos = c.getArgument("position", ParsedPosition.class);
                          double scalar = c.getArgument("scalar", Double.class);
                          return launchAt(c, () -> pos.apply(c.getSource()), scalar);
                        })
                    )
                )
            )
        );
  }

  int launchAt(CommandContext<CommandSource> c, Supplier<Location> destination, double scalar)
      throws CommandSyntaxException
  {
    List<Entity> entities = ArgumentTypes.getEntities(c, "entity");

    for (Entity entity : entities) {
      var destLoc = destination.get().toVector();
      var loc = entity.getLocation().toVector();

      var vel = destLoc.clone()
          .subtract(loc)
          .normalize()
          .multiply(scalar);

      if (NumberConversions.isFinite(vel.getX())
          && NumberConversions.isFinite(vel.getY())
          && NumberConversions.isFinite(vel.getZ())
      ) {
        entity.setVelocity(vel);
      }
    }

    if (entities.size() == 1) {
      c.getSource().sendSuccess(
          Text.format("Launched &e{0}&r.", entities.get(0).teamDisplayName())
      );
    } else {
      c.getSource().sendSuccess(
          Text.format("Launched &e{0, number}&r entities.", entities.size())
      );
    }

    return 0;
  }

  int launchVelocityGiven(CommandContext<CommandSource> c, boolean add)
      throws CommandSyntaxException
  {
    ParsedPosition pos = c.getArgument("vec", ParsedPosition.class);

    if (pos.getType() != Type.LOCAL) {
      if (pos.getXCoordinate().relative()
          || pos.getYCoordinate().relative()
          || pos.getZCoordinate().relative()
      ) {
        throw Exceptions.create("Cannot use relative ('^') coordinates here");
      }
    }

    Location sourceLocation = c.getSource().getLocation();
    sourceLocation.setX(0);
    sourceLocation.setY(0);
    sourceLocation.setZ(0);
    pos.apply(sourceLocation);

    return launch(c, sourceLocation.toVector(), add);
  }

  int launch(CommandContext<CommandSource> c, Vector velocity, boolean add)
      throws CommandSyntaxException
  {
    Collection<Entity> entities = ArgumentTypes.getEntities(c, "entity");

    for (Entity e : entities) {
      if (add) {
        e.setVelocity(e.getVelocity().add(velocity.clone()));
      } else {
        e.setVelocity(velocity.clone());
      }
    }

    c.getSource().sendSuccess(text("Launched " + entities.size() + " entities"));
    return 0;
  }
}