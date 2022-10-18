package net.forthecrown.commands.user;

import com.google.common.collect.Streams;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserShopData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.Locale;

import static net.forthecrown.text.Text.format;

class UserEarningsNode extends UserCommandNode {
    public UserEarningsNode() {
        super("user_earnings", "earnings");
        setAliases("userearnings");
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command, UserProvider provider) {
        command
                .then(literal("auto_sell")
                        .executes(c -> {
                            User user = provider.get(c);
                            UserShopData data = user.getComponent(UserShopData.class);

                            if (data.getAutoSelling().isEmpty()) {
                                throw Exceptions.NOTHING_TO_LIST;
                            }

                            c.getSource().sendMessage(
                                    format("{0, user}'s auto sell materials: {1}",
                                            user,
                                            TextJoiner.onComma()
                                                    .add(data.getAutoSelling()
                                                            .stream()
                                                            .map(material -> Component.text(material.name().toLowerCase()))
                                                    )
                                                    .asComponent()
                                    )
                            );
                            return 0;
                        })

                        .then(literal("clear")
                                .executes(c -> {
                                    User user = provider.get(c);
                                    UserShopData data = user.getComponent(UserShopData.class);

                                    data.getAutoSelling().clear();

                                    c.getSource().sendAdmin(
                                            format("Cleared {0, user}'s auto sell materials",
                                                    user
                                            )
                                    );
                                    return 0;
                                })
                        )

                        .then(literal("add")
                                .then(argument("material", Arguments.SELL_MATERIAL)
                                        .executes(c -> {
                                            User user = provider.get(c);
                                            var earnings = user.getComponent(UserShopData.class);
                                            var material = c.getArgument("material", Material.class);

                                            earnings.getAutoSelling().add(material);

                                            c.getSource().sendAdmin(
                                                    format("Added {0} to {1, user}'s auto sell list",
                                                            material, user
                                                    )
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("remove")
                                .then(argument("material", Arguments.SELL_MATERIAL)
                                        .executes(c -> {
                                            User user = provider.get(c);
                                            var earnings = user.getComponent(UserShopData.class);
                                            var material = c.getArgument("material", Material.class);

                                            earnings.getAutoSelling().remove(material);

                                            c.getSource().sendAdmin(
                                                    format("Removed {0} from {1, user}'s auto sell list",
                                                            material, user
                                                    )
                                            );
                                            return 0;
                                        })
                                )
                        )
                )

                .then(literal("earned")
                        .executes(c -> {
                            User user = provider.get(c);
                            var earnings = user.getComponent(UserShopData.class);

                            if (earnings.isEmpty()) {
                                throw Exceptions.NOTHING_TO_LIST;
                            }

                            c.getSource().sendMessage(
                                    format("{0, user}'s earned data:\n{1}",
                                            user,
                                            TextJoiner.onNewLine()
                                                    .add(
                                                            Streams.stream(earnings)
                                                                    .map(entry -> format("&7{0}&8:&r {1, rhines}",
                                                                            entry.getMaterial(),
                                                                            entry.getValue()
                                                                    ))
                                                    )
                                    )
                            );
                            return 0;
                        })

                        .then(literal("clear")
                                .executes(c -> {
                                    User user = provider.get(c);
                                    var earnings = user.getComponent(UserShopData.class);

                                    earnings.clear();

                                    c.getSource().sendAdmin(format("Cleared {0, user}'s earnings", user));
                                    return 0;
                                })
                        )

                        .then(literal("add")
                                .then(argument("material", Arguments.SELL_MATERIAL)
                                        .then(argument("amount", IntegerArgumentType.integer(1))
                                                .executes(c -> {
                                                    User user = provider.get(c);
                                                    var earnings = user.getComponent(UserShopData.class);
                                                    var material = c.getArgument("material", Material.class);
                                                    int amount = c.getArgument("amount", Integer.class);

                                                    int newAmount = earnings.get(material) + amount;

                                                    c.getSource().sendAdmin(
                                                            format("Added {0, rhines} to {1} earnings of {2, user}, now {3, rhines}",
                                                                    amount, material, user, newAmount
                                                            )
                                                    );
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(literal("remove")
                                .then(argument("material", Arguments.SELL_MATERIAL)
                                        .executes(c -> {
                                            User user = provider.get(c);
                                            var earnings = user.getComponent(UserShopData.class);
                                            var material = c.getArgument("material", Material.class);

                                            earnings.remove(material);

                                            c.getSource().sendAdmin(removedMessage(material, user));
                                            return 0;
                                        })

                                        .then(argument("amount", IntegerArgumentType.integer(1))
                                                .executes(c -> {
                                                    User user = provider.get(c);
                                                    var earnings = user.getComponent(UserShopData.class);
                                                    var material = c.getArgument("material", Material.class);
                                                    int amount = c.getArgument("amount", Integer.class);

                                                    int current = earnings.get(material);
                                                    int newAmount = current - amount;

                                                    earnings.set(material, newAmount);

                                                    if (newAmount <= 0) {
                                                        c.getSource().sendAdmin(removedMessage(material, user));
                                                    } else {
                                                        c.getSource().sendAdmin(
                                                                format("Decremented {0} earnings of {1, user} " +
                                                                                "by {2, rhines}, now: {3, rhines}",
                                                                        material.name().toLowerCase(),
                                                                        user,
                                                                        amount, newAmount
                                                                )
                                                        );
                                                    }

                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(literal("set")
                                .then(argument("material", Arguments.SELL_MATERIAL)
                                        .then(argument("amount", IntegerArgumentType.integer(0))
                                                .executes(c -> {
                                                    User user = provider.get(c);
                                                    var earnings = user.getComponent(UserShopData.class);
                                                    var material = c.getArgument("material", Material.class);
                                                    int amount = c.getArgument("amount", Integer.class);

                                                    earnings.set(material, amount);

                                                    if (amount == 0) {
                                                        c.getSource().sendAdmin(removedMessage(material, user));
                                                    } else {
                                                        c.getSource().sendAdmin(
                                                                format("Set {0} earnings of {1, user} to {2, rhines}",
                                                                        material.name().toLowerCase(Locale.ROOT),
                                                                        user, amount
                                                                )
                                                        );
                                                    }
                                                    return 0;
                                                })
                                        )
                                )
                        )
                );
    }

    Component removedMessage(Material material, User user) {
        return format("Removed {0} earnings from {1, user}",
                material.name().toLowerCase(), user
        );
    }
}