package net.forthecrown.core.commands.admin;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ParsedPosition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(argument("entity", ArgumentTypes.entities())
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
    );
  }

  int launchVelocityGiven(CommandContext<CommandSource> c,
                          boolean add
  ) throws CommandSyntaxException {
    ParsedPosition pos = c.getArgument("vec", ParsedPosition.class);

    if (pos.getXCoordinate().relative()
        || pos.getYCoordinate().relative()
        || pos.getZCoordinate().relative()
    ) {
      throw Exceptions.create("Cannot use relative ('~' or '^') coordiantes here");
    }

    return launch(
        c,
        new Vector(
            pos.getXCoordinate().value(),
            pos.getYCoordinate().value(),
            pos.getZCoordinate().value()
        ),
        add
    );
  }

  int launch(CommandContext<CommandSource> c,
             Vector velocity,
             boolean add
  ) throws CommandSyntaxException {
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