package net.forthecrown.commands.punish;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Bounds3i;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3d;

public class CommandJails extends FtcCommand {

    public CommandJails() {
        super("Jails");

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
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    TextWriter writer = TextWriters.newWriter();
                    writer.write(Component.text("Jails: ["));
                    writer.newLine();

                    for (var holder: Registries.JAILS.entries()) {
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

                                    if (Registries.JAILS.contains(k)) {
                                        throw Exceptions.jailExists(k);
                                    }

                                    World w = user.getWorld();
                                    Vector3d pos = Vectors.doubleFrom(user.getLocation());
                                    Bounds3i cell = Bounds3i.of(region);

                                    if (!cell.contains(pos.toInt())) {
                                        throw Exceptions.INVALID_JAIL_SPAWN;
                                    }

                                    JailCell jailCell = new JailCell(w, pos, cell);
                                    Registries.JAILS.register(k, jailCell);

                                    c.getSource().sendAdmin("Created jail cell: " + k);
                                    return 0;
                                })
                        )
                )

                .then(literal("delete")
                        .then(argument("jail", RegistryArguments.JAIL_CELL)
                                .executes(c -> {
                                    Holder<JailCell> cell = c.getArgument("jail", Holder.class);
                                    Registries.JAILS.remove(cell.getKey());

                                    c.getSource().sendAdmin("Removed jail " + cell.getKey());
                                    return 0;
                                })
                        )
                );
    }
}