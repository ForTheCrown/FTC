package net.forthecrown.commands;

import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ListUtils;

public class CommandCosmeticEffects extends FtcCommand {

    public CommandCosmeticEffects() {
        super("cosmeticeffect");

        setPermission(Permissions.FTC_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /cosmeticeffect <user> <death | arrow | travel> <effect key> <add | remove | set>
     * /cosmeticeffect <user> <death | arrow | travel> unset
     *
     *
     * Permissions used:
     * ftc.core.admin
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .then(literal("death")
                                .then(literal("unset")
                                        .executes(c -> {
                                            CrownUser user = UserArgument.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            data.setActiveDeath(null);

                                            c.getSource().sendAdmin("Unset " + user.getName() + "'s death particle");
                                            return 0;
                                        })
                                )

                                .then(literal("list")
                                        .executes(c -> {
                                            CrownUser user = UserArgument.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            String msg = ListUtils.join(data.getDeathEffects(), effect -> effect.key().value());

                                            c.getSource().sendMessage(user.getName() + "'s death effects: " + msg);
                                            return 0;
                                        })
                                )

                                .then(argument("key", RegistryArguments.deathEffect())
                                        .then(literal("add")
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    DeathEffect effect = c.getArgument("key", DeathEffect.class);
                                                    data.addDeath(effect);

                                                    c.getSource().sendAdmin("Added " + effect + " to " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("remove")
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    DeathEffect effect = c.getArgument("key", DeathEffect.class);
                                                    data.removeDeath(effect);

                                                    c.getSource().sendAdmin("Removed " + effect + " from " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("set")
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    DeathEffect effect = c.getArgument("key", DeathEffect.class);
                                                    data.setActiveDeath(effect);

                                                    c.getSource().sendAdmin("Set " + user.getName() + "'s active death to " + effect);
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(literal("travel")
                                .then(literal("unset")
                                        .executes(c -> {
                                            CrownUser user = UserArgument.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            data.setActiveTravel(null);

                                            c.getSource().sendAdmin("Unset " + user.getName() + "'s travel effect");
                                            return 0;
                                        })
                                )

                                .then(literal("list")
                                        .executes(c -> {
                                            CrownUser user = UserArgument.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            String msg = ListUtils.join(data.getTravelEffects(), effect -> effect.key().value());

                                            c.getSource().sendMessage(user.getName() + "'s travel effects: " + msg);
                                            return 0;
                                        })
                                )

                                .then(argument("key", RegistryArguments.travelEffect())
                                        .then(literal("add")
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    TravelEffect effect = c.getArgument("key", TravelEffect.class);
                                                    data.addTravel(effect);

                                                    c.getSource().sendAdmin("Added " + effect + " to " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("remove")
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    TravelEffect effect = c.getArgument("key", TravelEffect.class);
                                                    data.removeTravel(effect);

                                                    c.getSource().sendAdmin("Removed " + effect + " from " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("set")
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    TravelEffect effect = c.getArgument("key", TravelEffect.class);
                                                    data.setActiveTravel(effect);

                                                    c.getSource().sendAdmin("Set " + user.getName() + "'s active travel effect to " + effect);
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(literal("arrow")
                                .then(literal("unset")
                                        .executes(c -> {
                                            CrownUser user = UserArgument.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            data.setActiveArrow(null);

                                            c.getSource().sendAdmin("Unset " + user.getName() + "'s arrow particle");
                                            return 0;
                                        })
                                )

                                .then(literal("list")
                                        .executes(c -> {
                                            CrownUser user = UserArgument.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            String msg = ListUtils.join(data.getArrowEffects(), effect -> effect.key().value());

                                            c.getSource().sendMessage(user.getName() + "'s arrow effects: " + msg);
                                            return 0;
                                        })
                                )

                                .then(argument("key", RegistryArguments.arrowEffect())
                                        .then(literal("add")
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    ArrowEffect effect = c.getArgument("key", ArrowEffect.class);
                                                    data.addArrow(effect);

                                                    c.getSource().sendAdmin("Added " + effect + " to " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("remove")
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    ArrowEffect effect = c.getArgument("key", ArrowEffect.class);
                                                    data.removeArrow(effect);

                                                    c.getSource().sendAdmin("Removed " + effect + " from " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("set")
                                                .executes(c -> {
                                                    CrownUser user = UserArgument.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    ArrowEffect effect = c.getArgument("key", ArrowEffect.class);
                                                    data.setActiveArrow(effect);

                                                    c.getSource().sendAdmin("Set " + user.getName() + "'s active arrow to " + effect);
                                                    return 0;
                                                })
                                        )
                                )
                        )
                );
    }
}