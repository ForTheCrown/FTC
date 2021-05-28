package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.Announcer;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.arguments.WarpType;
import net.forthecrown.emperor.useables.warps.Warp;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.UserTeleport;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.emperor.utils.InterUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public class CommandWarpEdit extends CrownCommandBuilder {
    public CommandWarpEdit(){
        super("warpedit", CrownCore.inst());

        setPermission(Permissions.WARP_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("create")
                        .then(argument("name", StringArgumentType.word())
                                .executes(c -> createWarp(
                                        c.getSource(),
                                        c.getArgument("name", String.class),
                                        c.getSource().asPlayer().getLocation()
                                ))

                                .then(argument("location", PositionArgument.position())
                                        .executes(c -> createWarp(
                                                c.getSource(),
                                                c.getArgument("name", String.class),
                                                c.getArgument("location", Position.class).getLocation(c.getSource())
                                        ))
                                )
                        )
                )

                .then(argument("warp", WarpType.warp())
                        .suggests((c, b) -> WarpType.warp().listSuggestions(c, b, true))

                        .then(argument("destination")
                                .executes(c -> moveDest(
                                        c.getSource(),
                                        get(c),
                                        c.getSource().asPlayer().getLocation()
                                ))

                                .then(argument("dest", PositionArgument.position())
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            return moveDest(source, get(c), c.getArgument("dest", Position.class).getLocation(source));
                                        })
                                )
                        )

                        .then(argument("goto")
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    Warp warp = get(c);
                                    Announcer.debug(warp);

                                    user.createTeleport(warp::getDestination, true, UserTeleport.Type.WARP).start(true);
                                    return 0;
                                })
                        )

                        .then(InterUtils.checksArguments(this::get))

                        .then(argument("delete")
                                .executes(c -> {
                                    Warp warp = get(c);
                                    warp.delete();

                                    c.getSource().sendAdmin(Component.text("Deleting warp ").append(warp.displayName()));
                                    return 0;
                                })
                        )
                );
    }

    private int createWarp(CommandSource c, String name, Location location) throws CommandSyntaxException {
        Warp warp = CrownCore.getWarpRegistry().register(CrownUtils.parseKey(name), location);
        c.sendAdmin(Component.text("Creating warp named ").append(warp.displayName()));
        return 0;
    }

    public int moveDest(CommandSource source, Warp warp, Location location){
        warp.setDestination(location);

        source.sendAdmin(Component.text("Moved destination of ").append(warp.displayName()));
        return 0;
    }

    public Warp get(CommandContext<CommandSource> c){
        return WarpType.getWarp(c, "warp");
    }
}
