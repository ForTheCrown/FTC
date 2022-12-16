package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.text.format.UnitFormat;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.UUID2IntMap;
import net.kyori.adventure.text.Component;

import static net.forthecrown.utils.text.Text.format;

public class UserMapCommand extends FtcCommand {

    private final UUID2IntMap map;
    private final Long2ObjectFunction<Component> displayProvider;

    public UserMapCommand(String name,
                          UUID2IntMap map,
                          Long2ObjectFunction<Component> displayProvider,
                          String... aliases
    ) {
        super(name);

        this.map = map;
        this.displayProvider = displayProvider;

        setAliases(aliases);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /<name>
     * /<name> <user>
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                // /<command>
                .executes(c -> lookup(c.getSource(), getUserSender(c)))

                // /<command> <user>
                .then(argument("user", Arguments.USER)
                        .executes(c -> {
                            var user = Arguments.getUser(c, "user");
                            return lookup(c.getSource(), user);
                        })

                        // /<command> <user> set <amount>
                        .then(literal("set")
                                .requires(source -> source.hasPermission(Permissions.ADMIN))

                                .then(argument("amount", IntegerArgumentType.integer(0))
                                        .executes(c -> {
                                            var user = Arguments.getUser(c, "user");
                                            int amount = c.getArgument("amount", Integer.class);

                                            map.set(user.getUniqueId(), amount);
                                            int newAmount = map.get(user.getUniqueId());

                                            c.getSource().sendAdmin(
                                                    format("Set {0, user}'s {1} to {2}",
                                                            user,
                                                            getName(),
                                                            displayProvider.apply(newAmount)
                                                    )
                                            );
                                            return 0;
                                        })
                                )
                        )

                        // /<command> <user> add <amount>
                        .then(literal("add")
                                .requires(source -> source.hasPermission(Permissions.ADMIN))

                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(c -> {
                                            var user = Arguments.getUser(c, "user");
                                            int amount = c.getArgument("amount", Integer.class);

                                            map.add(user.getUniqueId(), amount);
                                            int newAmount = map.get(user.getUniqueId());

                                            c.getSource().sendAdmin(
                                                    format("Added {0} to {1, user}'s {2}, new value: {3}",
                                                            displayProvider.apply(amount),
                                                            user,
                                                            getName(),
                                                            displayProvider.apply(newAmount)
                                                    )
                                            );
                                            return 0;
                                        })
                                )
                        )

                        // /<command> <user> subtract <amount>
                        .then(literal("subtract")
                                .requires(source -> source.hasPermission(Permissions.ADMIN))

                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(c -> {
                                            var user = Arguments.getUser(c, "user");
                                            int amount = c.getArgument("amount", Integer.class);

                                            map.remove(user.getUniqueId(), amount);
                                            int newAmount = map.get(user.getUniqueId());

                                            c.getSource().sendAdmin(
                                                    format("Subtracted {0} from {1, user}'s {2}, new value: {3}",
                                                            displayProvider.apply(amount),
                                                            user,
                                                            getName(),
                                                            displayProvider.apply(newAmount)
                                                    )
                                            );
                                            return 0;
                                        })
                                )
                        )

                        // /<command> <user> remove
                        // This will remove the given user from the map
                        // completely, as opposed to the subtract argument,
                        // which simply decrements the given user's value
                        // in the map
                        .then(literal("remove")
                                .requires(source -> source.hasPermission(Permissions.ADMIN))

                                .executes(c -> {
                                    var user = Arguments.getUser(c, "user");
                                    map.remove(user.getUniqueId());

                                    c.getSource().sendAdmin(
                                            format("Removed {0, user} from {1} list", user, getName())
                                    );
                                    return 0;
                                })
                        )
                );
    }

    private int lookup(CommandSource source, User user) {
        var val = map.get(user.getUniqueId());
        var self = source.textName().equals(user.getName());

        if (self) {
            source.sendMessage(
                    Messages.unitQuerySelf(displayProvider.apply(val))
            );
        } else {
            source.sendMessage(
                    Messages.unitQueryOther(
                            displayProvider.get(val),
                            user
                    )
            );
        }

        return 0;
    }

    public static void createCommands() {
        var users = UserManager.get();

        new UserMapCommand(
                "balance",
                users.getBalances(),
                UnitFormat::rhines,
                "bal", "bank", "cash", "money", "ebal"
        );

        new UserMapCommand(
                "gems",
                users.getGems(),
                UnitFormat::gems
        );

        new UserMapCommand(
                "votes",
                users.getVotes(),
                UnitFormat::votes
        );

        new UserMapCommand(
                "playtime",
                users.getPlayTime(),
                UnitFormat::playTime
        );
    }
}