package net.forthecrown.commands;

import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.utils.animation.BlockAnimation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CommandAnimation extends FtcCommand {

    public CommandAnimation() {
        super("animation");

        setPermission(Permissions.FTC_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("animation", RegistryArguments.animation())
                        .then(argument("pos", PositionArgument.position())
                                .executes(c -> playAnimation(
                                        PositionArgument.getLocation(c, "pos"),
                                        c.getSource(),
                                        c.getArgument("animation", BlockAnimation.class)
                                ))
                        )

                        .executes(c -> {
                            Player player = c.getSource().asPlayer();

                            return playAnimation(
                                    player.getLocation(),
                                    c.getSource(),
                                    c.getArgument("animation", BlockAnimation.class)
                            );
                        })
                );
    }

    private int playAnimation(Location loc, CommandSource source, BlockAnimation animation) {
        animation.play(loc);

        source.sendMessage("Playing animation: " + animation.key());
        return 0;
    }
}