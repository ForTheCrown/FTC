package net.forthecrown.commands;

import com.google.common.collect.Collections2;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;

public class CommandNear extends FtcCommand {

    public CommandNear(){
        super("near");

        setAliases("nearby");
        setPermission(Permissions.NEARBY);
        setDescription("Shows nearby players");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);
                    return showNearby(user.getLocation(), GeneralConfig.nearRadius, c.getSource());
                })

                .then(argument("radius", IntegerArgumentType.integer(1, 100000))
                        .requires(s -> s.hasPermission(Permissions.NEARBY_ADMIN))

                        .executes(c -> {
                            User user = getUserSender(c);
                            return showNearby(user.getLocation(), c.getArgument("radius", Integer.class), c.getSource());
                        })
                )

                .then(argument("user", Arguments.ONLINE_USER)
                        .requires(s -> s.hasPermission(Permissions.NEARBY_ADMIN))

                        .executes(c -> {
                            User user = Arguments.getUser(c, "user");

                            return showNearby(user.getLocation(), GeneralConfig.nearRadius, c.getSource());
                        })

                        .then(argument("radius", IntegerArgumentType.integer(1, 100000))
                                .requires(s -> s.hasPermission(Permissions.NEARBY_ADMIN))

                                .executes(c -> {
                                    User user = Arguments.getUser(c, "user");
                                    int radius = c.getArgument("radius", Integer.class);

                                    return showNearby(user.getLocation(), radius, c.getSource());
                                })
                        )
                );
    }

    private int showNearby(Location loc, int radius, CommandSource source) throws CommandSyntaxException {
        var players = Collections2.transform(loc.getNearbyPlayers(radius), Users::get);

        players.removeIf(user -> user.hasPermission(Permissions.NEARBY_IGNORE)
                || user.getGameMode() == GameMode.SPECTATOR
                || user.getName().equalsIgnoreCase(source.textName())
                || user.get(Properties.PROFILE_PRIVATE)
                || user.get(Properties.VANISHED)
        );

        if (players.isEmpty()) {
            throw Exceptions.NO_NEARBY_PLAYERS;
        }

        TextComponent.Builder builder = Component.text()
                .append(Messages.NEARBY_HEADER);

        builder.append(
                TextJoiner.onComma().add(
                                players.stream()
                                        .map(user -> Text.format("{0, user} &7({1})",
                                                NamedTextColor.YELLOW,
                                                user,
                                                dist(user.getLocation(), loc)
                                        ))
                        )
                        .asComponent()
        );

        source.sendMessage(builder.build());
        return 0;
    }

    private String dist(Location from, Location to) {
        return Math.floor(from.distance(to)) + "b";
    }
}