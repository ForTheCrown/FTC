package net.forthecrown.vikings.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.blessings.VikingBlessing;
import net.forthecrown.vikings.inventory.BlessingSelector;
import net.forthecrown.vikings.inventory.RaidSelector;
import net.forthecrown.vikings.raids.RaidHandler;
import net.forthecrown.vikings.raids.VikingRaid;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.entity.Player;

public class CommandViking extends CrownCommandBuilder {

    public CommandViking() {
        super("viking", Vikings.getInstance());

        setPermission("ftc.vikings.admin");
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .then(argument("reload")
                        .executes(c -> {
                            Vikings.reloadVikings();
                            getSender(c).sendMessage("Vikings reloaded");
                            return 0;
                        })
                )
                .then(argument("save")
                        .executes(c ->{
                            Vikings.saveVikings();
                            getSender(c).sendMessage("Vikings saved");
                            return 0;
                        })
                )

                .then(argument("raid")
                        .then(argument("start")
                                .then(argument("raid", StringArgumentType.word())
                                        .executes(c -> {
                                            Player p = getPlayerSender(c);

                                            VikingRaid raid = RaidHandler.fromName(c.getArgument("raid", String.class));
                                            if(raid == null) throw new CrownCommandException("Invalid raid!");

                                            Vikings.getRaidHandler().callRaid(p, raid);
                                            p.sendMessage("Starting raid!");
                                            return 0;
                                        })
                                )
                        )
                        .then(argument("stop")
                                .executes(c -> {
                                    Player p = getPlayerSender(c);

                                    VikingRaid raid = RaidHandler.fromPlayer(p);
                                    if(raid == null) throw new CrownCommandException("You are not currently in a raid!");

                                    raid.onRaidEnd();
                                    p.sendMessage("Ending raid");
                                    return 0;
                                })
                        )
                        .then(argument("complete")
                                .executes(c -> {
                                    Player p = getPlayerSender(c);

                                    VikingRaid raid = RaidHandler.fromPlayer(p);
                                    if(raid == null) throw new CrownCommandException("You are not currently in a raid!");

                                    raid.completeRaid();
                                    p.sendMessage("Ending raid as success");
                                    return 0;
                                })
                        )
                        .then(argument("inventory")
                                .executes(c->{
                                    CrownUser user = getUserSender(c);

                                    try {
                                        user.getPlayer().openInventory(new RaidSelector(user).getInventory());
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    return 0;
                                })
                        )
                )
                .then(argument("blessing")
                        .then(argument("blessingName", StringArgumentType.word())
                                .suggests((c, b) -> )

                                .then(argument("beginUsage")
                                        .executes(c -> {
                                            CrownUser user = getUserSender(c);

                                            VikingBlessing b = VikingBlessing.fromName(c.getArgument("blessingName", String.class));
                                            if(b == null) throw new CrownCommandException("Invalid blessing!");

                                            b.beginUsage(user);
                                            user.sendMessage("Starting blessing usage!");
                                            return 0;
                                        })
                                )
                                .then(argument("endUsage")
                                        .executes(c ->{
                                            CrownUser user = getUserSender(c);

                                            VikingBlessing b = VikingBlessing.fromName(c.getArgument("blessingName", String.class));
                                            if(b == null) throw new CrownCommandException("Invalid blessing!");

                                            b.endUsage(user);
                                            user.sendMessage("Stopping blessing usage!");
                                            return 0;
                                        })
                                )
                        )
                        .then(argument("inventory")
                                .executes(c->{
                                    CrownUser user = getUserSender(c);

                                    try {
                                        user.getPlayer().openInventory(new BlessingSelector(user).getInventory());
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    return 0;
                                })
                        )
                );
    }
}
