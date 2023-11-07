package net.forthecrown.antigrief.commands;

import net.forthecrown.antigrief.GExceptions;
import net.forthecrown.antigrief.GriefPermissions;
import net.forthecrown.antigrief.JailCell;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.utils.math.AbstractBounds3i;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3d;

public class CommandJails extends FtcCommand {

  public CommandJails() {
    super("Jails");

    setPermission(GriefPermissions.PUNISH_JAIL);
    setDescription("Lists all jails");
    setAliases("jaillist", "listjails");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /Jails
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          var writer = TextWriters.newWriter();
          writer.write(Component.text("Jails: ["));
          writer.newLine();

          var cells = Punishments.get().getCells();

          for (var holder : cells.entries()) {
            JailCell cell = holder.getValue();

            writer.write("{");
            TextWriter indented = writer.withIndent();
            indented.newLine();

            indented.field("Name", holder.getKey());
            cell.writeDisplay(indented);

            writer.line("}");
          }

          writer.newLine();
          writer.write(Component.text("]"));

          c.getSource().sendMessage(writer.asComponent());
          return 0;
        })

        .then(literal("create")
            .then(argument("key", Arguments.FTC_KEY)
                .executes(c -> {
                  var user = getUserSender(c);

                  String k = c.getArgument("key", String.class);
                  var cells = Punishments.get().getCells();

                  if (cells.contains(k)) {
                    throw GExceptions.jailExists(k);
                  }

                  World w = user.getWorld();
                  Vector3d pos = Vectors.doubleFrom(user.getLocation());
                  WorldBounds3i cell = WorldBounds3i.ofPlayerSelection(user.getPlayer());

                  if (cell == null) {
                    throw Exceptions.create(
                        "No //wand selection, select 2 areas to define the jail cell"
                    );
                  }

                  if (!cell.contains(pos.toInt())) {
                    throw GExceptions.INVALID_JAIL_SPAWN;
                  }

                  JailCell jailCell
                      = new JailCell(w, pos, Bounds3i.of((AbstractBounds3i<?>) cell));

                  cells.register(k, jailCell);

                  c.getSource().sendSuccess(
                      Component.text("Created jail cell: " + k)
                  );
                  return 0;
                })
            )
        )

        .then(literal("delete")
            .then(argument("jail", AntiGriefCommands.JAIL_CELL_ARG)
                .executes(c -> {
                  Holder<JailCell> cell = c.getArgument("jail", Holder.class);

                  Punishments.get().getCells()
                      .remove(cell.getKey());

                  c.getSource().sendSuccess(
                      Component.text("Removed jail " + cell.getKey())
                  );
                  return 0;
                })
            )
        );
  }
}