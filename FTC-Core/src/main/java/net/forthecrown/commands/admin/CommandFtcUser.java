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
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.actions.ActionFactory;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.user.data.SellAmount;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CommandFtcUser extends FtcCommand {

    public CommandFtcUser() {
        super("FtcUser");

        setPermission(Permissions.ADMIN);
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
                                    user.unload();
                                    user.delete();

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

                        .then(literal("createTeleport")
                                .then(argument("destination", PositionArgument.position())
                                        .executes(createTeleport(true))

                                        .then(literal("with_cooldown").executes(createTeleport(true)))
                                        .then(literal("no_cooldown").executes(createTeleport(false)))
                                )
                        )

                        .then(literal("reset_market_cooldown")
                                .executes(userCmd((user, c) -> {
                                    user.getMarketOwnership().setLastStatusChange(0L);

                                    c.getSource().sendAdmin("Reset " + user.getName() + "'s market status cooldown");
                                }))
                        )

                        .then(componentArg("prefix", CrownUser::getCurrentPrefix, CrownUser::setCurrentPrefix))
                        .then(componentArg("nickname", CrownUser::nickname, CrownUser::setNickname))

                        .then(userProperty(UserAccessor.SELL_AMOUNT))
                        .then(userProperty(UserAccessor.TIER))
                        .then(userProperty(UserAccessor.TITLE))

                        .then(listArgument(ListAccessor.TITLES))

                        .then(literal("cosmetics")
                                .then(cosmeticDataArg(CosmeticDataAccessor.ARROW))
                                .then(cosmeticDataArg(CosmeticDataAccessor.TRAVEL))
                                .then(cosmeticDataArg(CosmeticDataAccessor.DEATH))
                        )

                        .then(interactionsArg())
                );
    }

    static CrownUser get(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return UserArgument.getUser(c, "user");
    }
    public interface UserCommandConsumer extends Command<CommandSource> {
        void run(CrownUser user, CommandContext<CommandSource> c) throws CommandSyntaxException;

        @Override
        default int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
            CrownUser user = get(context);

            run(user, context);
            return 0;
        }
    }
    Command<CommandSource> userCmd(UserCommandConsumer consumer) {
        return consumer;
    }

    private LiteralArgumentBuilder<CommandSource> componentArg(String name,
                                                               Function<CrownUser, Component> getter,
                                                               BiConsumer<CrownUser, Component> setter
    ) {
        return CommandLore.compOrStringArg(
                literal(name.toLowerCase())
                        .executes(userCmd((user, c) -> {
                            Component text = getter.apply(user);
                            if(text == null) text = Component.text("null");

                            c.getSource().sendMessage(
                                    Component.text(user.getName() + "'s " + name + ": ")
                                            .append(text)
                            );
                        }))
                        .then(literal("-clear")
                                .executes(userCmd((user, c) -> {
                                    setter.accept(user, null);

                                    c.getSource().sendAdmin("Removed " + user.getName() + "'s " + name);
                                }))
                        )

                ,
                ((context, builder) -> builder.buildFuture()),
                (context, lore) -> {
                    CrownUser user = get(context);
                    setter.accept(user, lore);

                    context.getSource().sendAdmin(
                            Component.text("Set " + user.getName() + "'s " + name + " to ")
                                    .append(lore)
                    );
                    return 0;
                }
        );
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

    private <T> LiteralArgumentBuilder<CommandSource> listArgument(ListAccessor<T> accessor) {
        return literal(accessor.getName().toLowerCase() + "s")
                .then(argument("value", accessor.getArgumentType())
                        .then(literal("add")
                                .executes(userCmd((user, c) -> {
                                    T val = c.getArgument("value", accessor.getTypeClass());
                                    accessor.add(val, user);

                                    c.getSource().sendAdmin("Added " + accessor.toString(val) + " to " + user.getName() + "'s " + accessor.getName() + "s");
                                }))
                        )

                        .then(literal("remove")
                                .executes(userCmd((user, c) -> {
                                    T val = c.getArgument("value", accessor.getTypeClass());
                                    accessor.remove(val, user);

                                    c.getSource().sendAdmin("Removed " + accessor.toString(val) + " from " + user.getName() + "'s " + accessor.getName() + "s");
                                }))
                        )
                )

                .then(literal("list")
                        .executes(userCmd((user, c) -> {
                            Collection<T> values = accessor.list(user);
                            String str = ListUtils.join(values, accessor::toString);

                            c.getSource().sendMessage(user.getName() + "s " + accessor.getName() + "s: " + str);
                        }))
                );
    }
    private interface ListAccessor<T> {
        ListAccessor<RankTitle> TITLES = new ListAccessor<>() {
            @Override
            public Collection<RankTitle> list(CrownUser user) {
                return user.getAvailableTitles();
            }

            @Override
            public void add(RankTitle val, CrownUser user) {
                user.addTitle(val, true, false);
            }

            @Override
            public void remove(RankTitle val, CrownUser user) {
                user.removeTitle(val, false);
            }

            @Override
            public String toString(RankTitle value) {
                return value.name().toLowerCase();
            }

            @Override
            public ArgumentType<RankTitle> getArgumentType() {
                return EnumArgument.of(RankTitle.class);
            }

            @Override
            public Class<RankTitle> getTypeClass() {
                return RankTitle.class;
            }

            @Override
            public String getName() {
                return "Title";
            }
        };

        Collection<T> list(CrownUser user);

        void add(T val, CrownUser user);
        void remove(T val, CrownUser user);

        String toString(T value);
        ArgumentType<T> getArgumentType();
        Class<T> getTypeClass();

        String getName();
    }

    private <T> LiteralArgumentBuilder<CommandSource> userProperty(UserAccessor<T> acc) {
        return literal(acc.getName().toLowerCase())
                .executes(userCmd((user, c) -> {
                    T val = acc.get(user);

                    c.getSource().sendMessage(user.getName() + "'s " + acc.getName() + ": " + acc.toString(val));
                }))

                .then(argument("value", acc.getArgumentType())
                        .executes(userCmd((user, c) -> {
                            T val = c.getArgument("value", acc.getTypeClass());
                            acc.set(val, user);

                            c.getSource().sendAdmin("Set " + user.getName() + "'s" + acc.getName() + " to " + acc.toString(val));
                        }))
                );
    }
    private interface UserAccessor<T> {
        UserAccessor<RankTitle> TITLE = new UserAccessor<RankTitle>() {
            @Override
            public void set(RankTitle val, CrownUser user) {
                user.setTitle(val);
            }

            @Override
            public RankTitle get(CrownUser user) {
                return user.getTitle();
            }

            @Override
            public String toString(RankTitle val) {
                return val.name().toLowerCase();
            }

            @Override
            public ArgumentType<RankTitle> getArgumentType() {
                return EnumArgument.of(RankTitle.class);
            }

            @Override
            public Class<RankTitle> getTypeClass() {
                return RankTitle.class;
            }

            @Override
            public String getName() {
                return "Title";
            }
        };

        UserAccessor<RankTier> TIER = new UserAccessor<RankTier>() {
            @Override
            public void set(RankTier val, CrownUser user) {
                user.setRankTier(val, true);
            }

            @Override
            public RankTier get(CrownUser user) {
                return user.getRankTier();
            }

            @Override
            public String toString(RankTier val) {
                return val.name().toLowerCase();
            }

            @Override
            public ArgumentType<RankTier> getArgumentType() {
                return EnumArgument.of(RankTier.class);
            }

            @Override
            public Class<RankTier> getTypeClass() {
                return RankTier.class;
            }

            @Override
            public String getName() {
                return "Tier";
            }
        };

        UserAccessor<SellAmount> SELL_AMOUNT = new UserAccessor<SellAmount>() {
            @Override
            public void set(SellAmount val, CrownUser user) {
                user.setSellAmount(val);
            }

            @Override
            public SellAmount get(CrownUser user) {
                return user.getSellAmount();
            }

            @Override
            public String toString(SellAmount val) {
                return val.text;
            }

            @Override
            public ArgumentType<SellAmount> getArgumentType() {
                return EnumArgument.of(SellAmount.class);
            }

            @Override
            public Class<SellAmount> getTypeClass() {
                return SellAmount.class;
            }

            @Override
            public String getName() {
                return getTypeClass().getSimpleName();
            }
        };

        void set(T val, CrownUser user);
        T get(CrownUser user);

        String toString(T val);
        ArgumentType<T> getArgumentType();
        Class<T> getTypeClass();
        String getName();
    }

    private UserCommandConsumer createTeleport(boolean cooldown) {
        return (user, c) -> {
            if(!user.isOnline()) {
                throw FtcExceptionProvider.create(user.getName() + " is not online");
            }

            Location dest = PositionArgument.getLocation(c, "destination");
            user.createTeleport(() -> dest, true, !cooldown, UserTeleport.Type.TELEPORT)
                    .start(true);
        };
    }

    private LiteralArgumentBuilder<CommandSource> interactionsArg() {
        return literal("interactions")
                .then(literal("marry")
                        .then(argument("user_1", UserArgument.user())
                                .executes(userCmd((user, c) -> {
                                    CrownUser target = UserArgument.getUser(c, "user_1");

                                    if(target.equals(user)) {
                                        throw FtcExceptionProvider.create("Cannot make a user marry themselves");
                                    }

                                    ActionFactory.marry(user, target, true);
                                    c.getSource().sendAdmin("Married " + user.getName() + " and " + target.getName());
                                }))
                        )
                )

                .then(literal("divorce")
                        .executes(userCmd((user, c) -> {
                            UserInteractions interactions = user.getInteractions();
                            if(!interactions.isMarried()) {
                                throw FtcExceptionProvider.create(user.getName() + " is not married");
                            }

                            ActionFactory.divorce(user, true);
                            c.getSource().sendAdmin(user.getName() + " is now divorced");
                        }))
                )

                .then(literal("reset_cooldown")
                        .executes(userCmd((user, c) -> {
                            user.getInteractions().setLastMarriageChange(0L);
                            c.getSource().sendAdmin("Reset " + user.getName() + "'s marriage cooldown");
                        }))
                );
    }
}