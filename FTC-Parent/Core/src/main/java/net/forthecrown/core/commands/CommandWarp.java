package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.arguments.UserType;
import net.forthecrown.core.commands.arguments.WarpType;
import net.forthecrown.core.useables.warps.Warp;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.data.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandWarp extends FtcCommand {

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
                                                Component.translatable("warps.to", warp.displayName().color(NamedTextColor.GOLD)).color(NamedTextColor.GRAY)
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

                                    c.getSource().sendMessage(
                                            Component.text("Warping ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(user.nickDisplayName().color(NamedTextColor.GOLD))
                                                    .append(Component.text(" to "))
                                                    .append(warp.displayName().color(NamedTextColor.YELLOW))
                                    );
                                    return 0;
                                })
                        )
                );
    }
}
