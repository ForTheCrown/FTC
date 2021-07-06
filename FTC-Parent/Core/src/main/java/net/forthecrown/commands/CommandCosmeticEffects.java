package net.forthecrown.commands;

import net.forthecrown.commands.arguments.ArrowEffectType;
import net.forthecrown.commands.arguments.DeathEffectType;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.AbstractDeathEffect;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ListUtils;

public class CommandCosmeticEffects extends FtcCommand {

    public CommandCosmeticEffects() {
        super("cosmeticeffect");

        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /cosmeticeffect <user> <death | arrow> <effect key> <add | remove | set>
     * /cosmeticeffect <user> <death | arrow> unset
     *
     *
     * Permissions used:
     * ftc.core.admin
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.user())
                        .then(literal("death")
                                .then(literal("unset")
                                        .executes(c -> {
                                            CrownUser user = UserType.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            data.setActiveDeath(null);

                                            c.getSource().sendAdmin("Unset " + user.getName() + "'s death particle");
                                            return 0;
                                        })
                                )

                                .then(literal("list")
                                        .executes(c -> {
                                            CrownUser user = UserType.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            String msg = ListUtils.join(data.getDeathEffects(), effect -> effect.key().value());

                                            c.getSource().sendMessage(user.getName() + "'s death effects: " + msg);
                                            return 0;
                                        })
                                )

                                .then(argument("key", DeathEffectType.deathEffect())
                                        .then(literal("add")
                                                .executes(c -> {
                                                    CrownUser user = UserType.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    AbstractDeathEffect effect = c.getArgument("key", AbstractDeathEffect.class);
                                                    data.addDeath(effect);

                                                    c.getSource().sendAdmin("Added " + effect + " to " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("remove")
                                                .executes(c -> {
                                                    CrownUser user = UserType.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    AbstractDeathEffect effect = c.getArgument("key", AbstractDeathEffect.class);
                                                    data.removeDeath(effect);

                                                    c.getSource().sendAdmin("Removed " + effect + " from " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("set")
                                                .executes(c -> {
                                                    CrownUser user = UserType.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    AbstractDeathEffect effect = c.getArgument("key", AbstractDeathEffect.class);
                                                    data.setActiveDeath(effect);

                                                    c.getSource().sendAdmin("Set " + user.getName() + "'s active death to " + effect);
                                                    return 0;
                                                })
                                        )
                                )
                        )

                        .then(literal("arrow")
                                .then(literal("unset")
                                        .executes(c -> {
                                            CrownUser user = UserType.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            data.setActiveArrow(null);

                                            c.getSource().sendAdmin("Unset " + user.getName() + "'s arrow particle");
                                            return 0;
                                        })
                                )

                                .then(literal("list")
                                        .executes(c -> {
                                            CrownUser user = UserType.getUser(c, "user");
                                            CosmeticData data = user.getCosmeticData();

                                            String msg = ListUtils.join(data.getArrowEffects(), effect -> effect.key().value());

                                            c.getSource().sendMessage(user.getName() + "'s arrow effects: " + msg);
                                            return 0;
                                        })
                                )

                                .then(argument("key", ArrowEffectType.arrowEffect())
                                        .then(literal("add")
                                                .executes(c -> {
                                                    CrownUser user = UserType.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    ArrowEffect effect = c.getArgument("key", ArrowEffect.class);
                                                    data.addArrow(effect);

                                                    c.getSource().sendAdmin("Added " + effect + " to " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("remove")
                                                .executes(c -> {
                                                    CrownUser user = UserType.getUser(c, "user");
                                                    CosmeticData data = user.getCosmeticData();

                                                    ArrowEffect effect = c.getArgument("key", ArrowEffect.class);
                                                    data.removeArrow(effect);

                                                    c.getSource().sendAdmin("Removed " + effect + " from " + user.getName());
                                                    return 0;
                                                })
                                        )

                                        .then(literal("set")
                                                .executes(c -> {
                                                    CrownUser user = UserType.getUser(c, "user");
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