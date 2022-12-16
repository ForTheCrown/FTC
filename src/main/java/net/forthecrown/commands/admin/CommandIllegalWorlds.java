package net.forthecrown.commands.admin;

import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

public class CommandIllegalWorlds extends FtcCommand {

    public CommandIllegalWorlds() {
        super("IllegalWorlds");

        setPermission(Permissions.ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /IllegalWorlds
     *
     * Permissions used:
     *
     * Main Author: Jules
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    if (GeneralConfig.getIllegalWorlds().isEmpty()) {
                        throw Exceptions.NOTHING_TO_LIST;
                    }

                    c.getSource().sendMessage(
                            TextJoiner.onComma()
                                    .add(GeneralConfig.getIllegalWorlds().stream()
                                            .map(Component::text)
                                    )
                    );
                    return 0;
                })

                .then(literal("add")
                        .then(argument("world", WorldArgument.world())
                                .executes(c -> {
                                    var world = getWorld(c);
                                    GeneralConfig.illegalWorlds.add(world.getName());

                                    c.getSource().sendAdmin(
                                            Text.format("Added '{0}' to illegal worlds list", world.getName())
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("remove")
                        .then(argument("world", WorldArgument.world())
                                .executes(c -> {
                                    var world = getWorld(c);
                                    GeneralConfig.illegalWorlds.remove(world.getName());

                                    c.getSource().sendAdmin(
                                            Text.format("Removed '{0}' from illegal worlds list", world.getName())
                                    );
                                    return 0;
                                })
                        )
                );
    }

    private World getWorld(CommandContext<CommandSource> c) {
        return c.getArgument("world", World.class);
    }
}