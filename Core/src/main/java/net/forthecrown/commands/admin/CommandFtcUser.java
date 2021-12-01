package net.forthecrown.commands.admin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.Collection;

public class CommandFtcUser extends FtcCommand {

    public CommandFtcUser() {
        super("FtcUser");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /FtcUser
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .then(literal("save")
                                .executes(userCmd((user, c) -> {
                                    user.save();
                                    c.getSource().sendAdmin("Saved data of " + user.getName());
                                }))
                        )

                        .then(literal("reload")
                                .executes(userCmd((user, c) -> {
                                    user.reload();
                                    c.getSource().sendAdmin("Reloaded data of " + user.getName());
                                }))
                        )

                        .then(literal("delete")
                                .executes(userCmd((user, c) -> {
                                    user.delete();
                                    user.unload();

                                    c.getSource().sendAdmin("Deleted data of " + user.getName());
                                }))
                        )

                        .then(literal("balance")
                                .executes(userCmd((user, c) -> c.getSource().sendAdmin(
                                        Component.text()
                                                .append(user.displayName())
                                                .append(Component.text(" has "))
                                                .append(FtcFormatter.rhines(user.getBalance()))
                                                .build()
                                )))

                                .then(literal("add")
                                        .then(argument("amount", IntegerArgumentType.integer(1, ComVars.getMaxMoneyAmount()))
                                                .executes(userCmd((user, c) -> {
                                                    int amount = c.getArgument("amount", Integer.class);
                                                    user.addBalance(c.getArgument("amount", Integer.class));

                                                    c.getSource().sendAdmin(
                                                            Component.text("Added ")
                                                                    .append(FtcFormatter.rhines(amount))
                                                                    .append(Component.text(" to "))
                                                                    .append(user.displayName())
                                                                    .append(Component.text(", now has "))
                                                                    .append(FtcFormatter.rhines(user.getBalance()))
                                                    );
                                                }))
                                        )
                                )

                                .then(literal("remove")
                                        .then(argument("amount", IntegerArgumentType.integer(1, ComVars.getMaxMoneyAmount()))
                                                .executes(userCmd((user, c) -> {
                                                    int amount = c.getArgument("amount", Integer.class);
                                                    user.removeBalance(c.getArgument("amount", Integer.class));

                                                    c.getSource().sendAdmin(
                                                            Component.text("Took ")
                                                                    .append(FtcFormatter.rhines(amount))
                                                                    .append(Component.text(" from "))
                                                                    .append(user.displayName())
                                                                    .append(Component.text(", now has "))
                                                                    .append(FtcFormatter.rhines(user.getBalance()))
                                                    );
                                                }))
                                        )
                                )

                                .then(literal("set")
                                        .then(argument("amount", IntegerArgumentType.integer(0, ComVars.getMaxMoneyAmount()))
                                                .executes(userCmd((user, c) -> {
                                                    int amount = c.getArgument("amount", Integer.class);
                                                    user.setBalance(amount);

                                                    c.getSource().sendAdmin(
                                                            Component.text()
                                                                    .append(user.displayName())
                                                                    .append(Component.text(" now has "))
                                                                    .append(FtcFormatter.rhines(amount))
                                                                    .build()
                                                    );
                                                }))
                                        )
                                )

                                .then(literal("reset")
                                        .executes(userCmd((user, c) -> {
                                            Crown.getEconomy().getMap().remove(user.getUniqueId());

                                            c.getSource().sendAdmin("Reset balance of " + user.getName());
                                        }))
                                )
                        )

                        .then(literal("gems")
                                .executes(userCmd((user, c) -> c.getSource().sendMessage(
                                        Component.text()
                                                .append(user.displayName())
                                                .append(Component.text(" has "))
                                                .append(FtcFormatter.gems(user.getGems()))
                                                .build()
                                )))

                                .then(literal("add")
                                        .then(argument("amount", IntegerArgumentType.integer(1))
                                                .executes(userCmd((user, c) -> {
                                                    int amount =  c.getArgument("amount", Integer.class);
                                                    user.addGems(amount);

                                                    c.getSource().sendMessage(
                                                            Component.text("Gave ")
                                                                    .append(user.displayName())
                                                                    .append(Component.space())
                                                                    .append(FtcFormatter.gems(user.getGems()))
                                                    );
                                                }))
                                        )
                                )

                                .then(literal("remove")
                                        .then(argument("amount", IntegerArgumentType.integer(1))
                                                .executes(userCmd((user, c) -> {
                                                    int amount =  c.getArgument("amount", Integer.class);
                                                    user.addGems(-amount);

                                                    c.getSource().sendMessage(
                                                            Component.text("Took ")
                                                                    .append(FtcFormatter.gems(user.getGems()))
                                                                    .append(Component.text(" from "))
                                                                    .append(user.displayName())
                                                    );
                                                }))
                                        )
                                )

                                .then(literal("set")
                                        .then(argument("amount", IntegerArgumentType.integer())
                                                .executes(userCmd((user, c) -> {
                                                    int amount =  c.getArgument("amount", Integer.class);
                                                    user.setGems(amount);

                                                    c.getSource().sendAdmin(
                                                            Component.text()
                                                                    .append(user.displayName())
                                                                    .append(Component.text(" now has "))
                                                                    .append(FtcFormatter.gems(amount))
                                                                    .build()
                                                    );
                                                }))
                                        )
                                )
                        )

                        .then(
                                CommandLore.compOrStringArg(
                                        literal("prefix")
                                                .executes(userCmd((user, c) -> {
                                                    Component prefix = user.getCurrentPrefix();

                                                    if(prefix == null) throw FtcExceptionProvider.create(user.getName() + " has no prefix");

                                                    c.getSource().sendAdmin(
                                                            Component.text("Prefix of ")
                                                                    .append(user.displayName())
                                                                    .append(Component.text(" is "))
                                                                    .append(prefix)
                                                    );
                                                })),
                                        (c, b) -> b.buildFuture(),
                                        (context, lore) -> {
                                            CrownUser user = get(context);

                                            user.setCurrentPrefix(lore);

                                            context.getSource().sendAdmin(
                                                    Component.text("Set prefix of ")
                                                            .append(user.displayName())
                                                            .append(Component.text(" to "))
                                                            .append(lore)
                                            );
                                            return 0;
                                        }
                                )
                        )

                        .then(cosmeticsArg())
                );
    }

    CrownUser get(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return UserArgument.getUser(c, "user");
    }

    public interface UserCommandConsumer {
        void run(CrownUser user, CommandContext<CommandSource> c) throws CommandSyntaxException;
    }

    Command<CommandSource> userCmd(UserCommandConsumer consumer) {
        return c -> {
            CrownUser user = get(c);
            consumer.run(user, c);
            return 0;
        };
    }

    private LiteralArgumentBuilder<CommandSource> cosmeticsArg() {
        return literal("cosmetics")
                .then(cosmeticDataArg(CosmeticDataAccessor.ARROW))
                .then(cosmeticDataArg(CosmeticDataAccessor.TRAVEL))
                .then(cosmeticDataArg(CosmeticDataAccessor.DEATH));
    }

    private <T extends CosmeticEffect> LiteralArgumentBuilder<CommandSource> cosmeticDataArg(CosmeticDataAccessor<T> accessor) {
        String argName = accessor.getName().toLowerCase().replaceAll(" effect", "");

        return literal(argName)
                .executes(userCmd((user, c) -> {
                    CosmeticData data = user.getCosmeticData();
                    T set = accessor.get(data);
                    Collection<T> available = accessor.list(data);

                    if(set == null && available.isEmpty()) {
                        throw FtcExceptionProvider.create(user.getName() + " has no set or available " + accessor.getName() + 's');
                    }

                    TextComponent.Builder builder = Component.text()
                            .append(user.displayName())
                            .append(Component.text("'s " + accessor.getName() + "s:"));

                    if(set != null) {
                        builder
                                .append(Component.newline())
                                .append(Component.text("Active: " + set));
                    }

                    if(!available.isEmpty()) {
                        builder
                                .append(Component.newline())
                                .append(Component.text(ListUtils.join(available, CosmeticEffect::toString)));
                    }

                    c.getSource().sendMessage(builder.build());
                }))

                .then(literal("unset")
                        .executes(userCmd((user, c) -> {
                            CosmeticData data = user.getCosmeticData();
                            accessor.set(null, data);

                            c.getSource().sendAdmin("Unset " + accessor.getName() + " of " + user.getName());
                        }))
                )

                .then(literal("clear")
                        .executes(userCmd((user, c) -> {
                            CosmeticData data = user.getCosmeticData();
                            accessor.clear(data);

                            c.getSource().sendAdmin("Cleared " + accessor.getName() + "s of " + user.getName());
                        }))
                )

                .then(argument("effect", accessor.getArgumentType())
                        .then(literal("add")
                                .executes(userCmd((user, c) -> {
                                    CosmeticData data = user.getCosmeticData();
                                    T parsed = c.getArgument("effect", accessor.getCosmeticClass());

                                    accessor.add(parsed, data);

                                    c.getSource().sendAdmin(
                                            "Added " + parsed + " to " + user.getName() + "'s " + accessor.getName() + 's'
                                    );
                                }))
                        )

                        .then(literal("remove")
                                .executes(userCmd((user, c) -> {
                                    CosmeticData data = user.getCosmeticData();
                                    T parsed = c.getArgument("effect", accessor.getCosmeticClass());

                                    accessor.remove(parsed, data);

                                    c.getSource().sendAdmin(
                                            "Removed " + parsed + " from " + user.getName() + "'s " + accessor.getName() + 's'
                                    );
                                }))
                        )

                        .then(literal("set")
                                .executes(userCmd((user, c) -> {
                                    CosmeticData data = user.getCosmeticData();
                                    T parsed = c.getArgument("effect", accessor.getCosmeticClass());

                                    accessor.set(parsed, data);

                                    c.getSource().sendAdmin(
                                            "Set active " + accessor.getName() + " for " + user.getName() + " to " + parsed
                                    );
                                }))
                        )
                );
    }

    private interface CosmeticDataAccessor<T extends CosmeticEffect> {
        CosmeticDataAccessor<TravelEffect> TRAVEL = new CosmeticDataAccessor<>() {
            @Override
            public void add(TravelEffect effect, CosmeticData data) {
                data.addTravel(effect);
            }

            @Override
            public void remove(TravelEffect effect, CosmeticData data) {
                data.removeTravel(effect);
            }

            @Override
            public void set(TravelEffect effect, CosmeticData data) {
                data.setActiveTravel(effect);
            }

            @Override
            public void clear(CosmeticData data) {
                data.getTravelEffects().clear();
            }

            @Override
            public TravelEffect get(CosmeticData data) {
                return data.getActiveTravel();
            }

            @Override
            public Collection<TravelEffect> list(CosmeticData data) {
                return data.getTravelEffects();
            }

            @Override
            public String getName() {
                return "Travel Effect";
            }

            @Override
            public ArgumentType<TravelEffect> getArgumentType() {
                return RegistryArguments.travelEffect();
            }

            @Override
            public Class<TravelEffect> getCosmeticClass() {
                return TravelEffect.class;
            }
        };

        CosmeticDataAccessor<DeathEffect> DEATH = new CosmeticDataAccessor<>() {
            @Override
            public void add(DeathEffect effect, CosmeticData data) {
                data.addDeath(effect);
            }

            @Override
            public void remove(DeathEffect effect, CosmeticData data) {
                data.removeDeath(effect);
            }

            @Override
            public void set(DeathEffect effect, CosmeticData data) {
                data.setActiveDeath(effect);
            }

            @Override
            public void clear(CosmeticData data) {
                data.getDeathEffects().clear();
            }

            @Override
            public DeathEffect get(CosmeticData data) {
                return data.getActiveDeath();
            }

            @Override
            public Collection<DeathEffect> list(CosmeticData data) {
                return data.getDeathEffects();
            }

            @Override
            public String getName() {
                return "Death Effect";
            }

            @Override
            public ArgumentType<DeathEffect> getArgumentType() {
                return RegistryArguments.deathEffect();
            }

            @Override
            public Class<DeathEffect> getCosmeticClass() {
                return DeathEffect.class;
            }
        };

        CosmeticDataAccessor<ArrowEffect> ARROW = new CosmeticDataAccessor<>() {
            @Override
            public void add(ArrowEffect effect, CosmeticData data) {
                data.addArrow(effect);
            }

            @Override
            public void remove(ArrowEffect effect, CosmeticData data) {
                data.removeArrow(effect);
            }

            @Override
            public void set(ArrowEffect effect, CosmeticData data) {
                data.setActiveArrow(effect);
            }

            @Override
            public void clear(CosmeticData data) {
                data.getArrowEffects().clear();
            }

            @Override
            public ArrowEffect get(CosmeticData data) {
                return data.getActiveArrow();
            }

            @Override
            public Collection<ArrowEffect> list(CosmeticData data) {
                return data.getArrowEffects();
            }

            @Override
            public String getName() {
                return "Arrow Effect";
            }

            @Override
            public ArgumentType<ArrowEffect> getArgumentType() {
                return RegistryArguments.arrowEffect();
            }

            @Override
            public Class<ArrowEffect> getCosmeticClass() {
                return ArrowEffect.class;
            }
        };

        void add(T effect, CosmeticData data);
        void remove(T effect, CosmeticData data);
        void set(T effect, CosmeticData data);

        void clear(CosmeticData data);

        T get(CosmeticData data);
        Collection<T> list(CosmeticData data);

        String getName();
        ArgumentType<T> getArgumentType();
        Class<T> getCosmeticClass();
    }
}