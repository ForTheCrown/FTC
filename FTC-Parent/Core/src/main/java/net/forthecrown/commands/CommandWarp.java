package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.arguments.WarpArgument;
import net.forthecrown.useables.warps.Warp;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandWarp extends FtcCommand {

    public CommandWarp(){
        super("warp", ForTheCrown.inst());

        setPermission(Permissions.WARP);
        setDescription("Warps you to a warp");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("warp", WarpArgument.warp())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            Warp warp = WarpArgument.getWarp(c, "warp");

                            if(user.checkTeleporting() && warp.test(user.getPlayer())){
                                user.createTeleport(warp::getDestination, true, UserTeleport.Type.WARP)
                                        .setCompleteMessage(
                                                Component.translatable("warps.to", warp.displayName().color(NamedTextColor.GOLD)).color(NamedTextColor.GRAY)
                                        )
                                        .start(true);
                            }
                            return 0;
                        })

                        .then(argument("user", UserArgument.onlineUser())
                                .requires(s -> s.hasPermission(Permissions.WARP_ADMIN))

                                .executes(c -> {
                                    CrownUser user = UserArgument.getUser(c, "user");
                                    Warp warp = WarpArgument.getWarp(c, "warp");

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
