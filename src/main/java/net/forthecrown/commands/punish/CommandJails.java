package net.forthecrown.commands.punish;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3d;

public class CommandJails extends FtcCommand {

  public CommandJails() {
    super("Jails");

    setPermission(Permissions.PUNISH_JAIL);
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
          TextWriter writer = TextWriters.newWriter();
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
                  User user = getUserSender(c);
                  Region region = Util.getSelectionSafe(BukkitAdapter.adapt(user.getPlayer()));

                  String k = c.getArgument("key", String.class);
                  var cells = Punishments.get().getCells();

                  if (cells.contains(k)) {
                    throw Exceptions.jailExists(k);
                  }

                  World w = user.getWorld();
                  Vector3d pos = Vectors.doubleFrom(user.getLocation());
                  Bounds3i cell = Bounds3i.of(region);

                  if (!cell.contains(pos.toInt())) {
                    throw Exceptions.INVALID_JAIL_SPAWN;
                  }

                  JailCell jailCell = new JailCell(w, pos, cell);
                  cells.register(k, jailCell);

                  c.getSource().sendSuccess(
                      Component.text("Created jail cell: " + k)
                  );
                  return 0;
                })
            )
        )

        .then(literal("delete")
            .then(argument("jail", RegistryArguments.JAIL_CELL)
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