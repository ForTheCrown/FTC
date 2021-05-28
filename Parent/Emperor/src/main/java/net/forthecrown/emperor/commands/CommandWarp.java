package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.arguments.WarpType;
import net.forthecrown.emperor.useables.warps.Warp;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandWarp extends CrownCommandBuilder {

    public CommandWarp(){
        super("warp", CrownCore.inst());

        setPermission(Permissions.WARP);
        setDescription("Warps you to a warp");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("warp", WarpType.warp())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            Warp warp = WarpType.getWarp(c, "warp");

                            if(user.checkTeleporting() && warp.test(user.getPlayer())){
                                user.createTeleport(warp::getDestination, true, UserTeleport.Type.WARP)
                                        .setCompleteMessage(
                                                Component.text("Warping to ")
                                                        .color(NamedTextColor.GRAY)
                                                        .append(warp.displayName().color(NamedTextColor.GOLD))
                                        )
                                        .start(true);
                            }
                            return 0;
                        })

                        .then(argument("user", UserType.onlineUser())
                                .requires(s -> s.hasPermission(Permissions.WARP_ADMIN))

                                .executes(c -> {
                                    CrownUser user = UserType.getUser(c, "user");
                                    Warp warp = WarpType.getWarp(c, "warp");

                                    user.createTeleport(warp::getDestination, true, true, UserTeleport.Type.WARP)
                                            .start(true);

                                    c.getSource().sendAdmin(
                                            Component.text("Warping ")
                                                    .append(user.nickDisplayName())
                                                    .append(Component.text(" to "))
                                                    .append(warp.displayName())
                                    );
                                    return 0;
                                })
                        )
                );
    }
}
