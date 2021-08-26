package net.forthecrown.commands.punishments;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.arguments.JailArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class CommandJails extends FtcCommand {
    public CommandJails(){
        super("jails", Crown.inst());

        setPermission(Permissions.FTC_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .then(argument("name", StringArgumentType.word())
                                .executes(c -> createJail(
                                        getPlayerSender(c).getLocation(),
                                        c.getArgument("name", String.class),
                                        c.getSource(),
                                        90,
                                        0
                                ))

                                .then(argument("location", PositionArgument.position())
                                        .executes(c -> createJail(
                                                c.getArgument("location", Position.class).getLocation(c.getSource()),
                                                c.getArgument("name", String.class),
                                                c.getSource(),
                                                90,
                                                0
                                        ))

                                        .then(argument("yaw", FloatArgumentType.floatArg(-180, 180))
                                                .then(argument("pitch", FloatArgumentType.floatArg(-180, 180))
                                                        .executes(c -> createJail(
                                                                c.getArgument("location", Position.class).getLocation(c.getSource()),
                                                                c.getArgument("name", String.class),
                                                                c.getSource(),
                                                                c.getArgument("yaw", Float.class),
                                                                c.getArgument("pitch", Float.class)
                                                        ))
                                                )
                                        )
                                )
                        )
                )

                .then(argument("jail", JailArgument.jail())
                        .then(literal("location")
                                .executes(c -> {
                                    CommandSource source = c.getSource();
                                    Key key = get(c);
                                    Location l = Crown.getJailManager().get(key);

                                    source.sendMessage(
                                            Component.text("Location of ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(Component.text(key.value()).color(NamedTextColor.YELLOW))
                                                    .append(Component.text(" is "))
                                                    .append(FtcFormatter.clickableLocationMessage(l, true).color(NamedTextColor.GOLD))
                                    );
                                    return 0;
                                })

                                .then(argument("pos", PositionArgument.position())
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            Key key = get(c);
                                            Location l = c.getArgument("pos", Position.class).getLocation(source);

                                            Crown.getJailManager().register(key, l);
                                            source.sendAdmin(
                                                    Component.text("Set location of ")
                                                            .color(NamedTextColor.GRAY)
                                                            .append(Component.text(key.value()).color(NamedTextColor.YELLOW))
                                                            .append(Component.text(" to "))
                                                            .append(FtcFormatter.clickableLocationMessage(l, true).color(NamedTextColor.GOLD))
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("delete")
                                .executes(c -> {
                                    CommandSource source = c.getSource();
                                    Key key = get(c);

                                    Crown.getJailManager().remove(key);

                                    source.sendAdmin(
                                            Component.text("Deleted jail ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(Component.text(key.value()).color(NamedTextColor.YELLOW))
                                    );
                                    return 0;
                                })
                        )
                );
    }

    private Key get(CommandContext<CommandSource> c){
        return c.getArgument("jail", Key.class);
    }

    private int createJail(Location loc, String name, CommandSource source, float yaw, float pitch) {
        Key key = FtcUtils.parseKey(name);

        loc.setYaw(yaw);
        loc.setPitch(pitch);

        Crown.getJailManager().register(key, loc);

        source.sendAdmin(
                Component.text("Created jail ")
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(name).color(NamedTextColor.YELLOW))
                        .append(Component.text(" at "))
                        .append(FtcFormatter.clickableLocationMessage(loc, true).color(NamedTextColor.GOLD))
        );
        return 0;
    }
}