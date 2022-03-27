package net.forthecrown.commands.punish;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Keys;
import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.MathUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

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
                    ComponentWriter writer = ComponentWriter.normal();
                    writer.write(Component.text("Jails: ["));
                    writer.newLine();

                    for (JailCell cell: Registries.JAILS) {
                        writer.write(Component.text('{'));
                        ComponentWriter indented = writer.prefixedWriter(Component.text("  "));
                        indented.newLine();

                        cell.writeDisplay(indented);

                        writer.newLine();
                        writer.write(Component.text('}'));
                    }

                    writer.newLine();
                    writer.write(Component.text("]"));

                    c.getSource().sendMessage(writer.get());
                    return 0;
                })

                .then(literal("create")
                        .then(argument("key", Keys.argumentType())
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    Region region = FtcUtils.getSelectionSafe(BukkitAdapter.adapt(user.getPlayer()));

                                    Key k = KeyArgument.getKey(c, "key");

                                    if(Registries.JAILS.contains(k)) {
                                        throw FtcExceptionProvider.create("Jail named " + k + " already exists");
                                    }

                                    World w = user.getWorld();
                                    Vector3 pos = MathUtil.toWorldEdit(user.getLocation());
                                    Bounds3i cell = Bounds3i.of(region);

                                    if(!cell.contains(pos.toBlockPoint())) {
                                        throw FtcExceptionProvider.create("Jail spawn point (The place you're standing at) isn't inside the cell room");
                                    }

                                    JailCell jailCell = new JailCell(k);
                                    jailCell.setCell(cell);
                                    jailCell.setPos(pos);
                                    jailCell.setWorld(w);

                                    Registries.JAILS.register(k, jailCell);

                                    c.getSource().sendAdmin("Created jail cell: " + k);
                                    return 0;
                                })
                        )
                )

                .then(literal("delete")
                        .then(argument("jail", RegistryArguments.jailCell())
                                .executes(c -> {
                                    JailCell cell = c.getArgument("jail", JailCell.class);
                                    Registries.JAILS.remove(cell.key());

                                    c.getSource().sendAdmin("Removed jail " + cell.key());
                                    return 0;
                                })
                        )
                );
    }
}