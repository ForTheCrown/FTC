package net.forthecrown.july.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.forthecrown.july.EventConstants;
import net.forthecrown.july.JulyMain;
import net.forthecrown.july.ParkourEntry;
import net.forthecrown.july.ParkourEvent;
import net.forthecrown.july.items.EventItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class CommandJulyEvent extends FtcCommand {

    public CommandJulyEvent() {
        super("julyEvent", JulyMain.inst);

        setPermission("ftc.july");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Does stuff for the july event
     *
     * Valid usages of command:
     *
     * Permissions used:
     * ftc.july
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("start")
                        .then(argument("target", EntityArgument.player())
                                .then(literal("practise").executes(c -> start(getPlayer(c), true)))
                                .then(literal("real").executes(c -> start(getPlayer(c), false)))
                        )
                )

                .then(literal("ent_tag")
                        .then(argument("entity", EntityArgument.entity())
                                .executes(c -> {
                                    Entity entity = EntityArgument.getEntity(c, "entity");

                                    entity.getPersistentDataContainer().set(EventConstants.NPC_KEY, PersistentDataType.BYTE, (byte) 1);

                                    c.getSource().sendAdmin("Giving NPC tag");
                                    return 0;
                                })
                        )
                )

                .then(literal("end")
                        .then(argument("target", EntityArgument.player())
                                .executes(c -> {
                                    Player player = getPlayer(c);

                                    JulyMain.event.end(ParkourEvent.ENTRIES.get(player));
                                    return 0;
                                })
                        )
                )
                .then(literal("complete")
                        .then(argument("target", EntityArgument.player())
                                .executes(c -> {
                                    Player player = getPlayer(c);
                                    ParkourEntry entry = ParkourEvent.ENTRIES.get(player);

                                    JulyMain.event.end(entry);
                                    return 0;
                                })
                        )
                )
                .then(literal("end_practise")
                        .then(argument("target", EntityArgument.player())
                                .executes(c -> {
                                    Player player = getPlayer(c);

                                    JulyMain.event.endPractise(player);
                                    return 0;
                                })
                        )
                )

                .then(literal("config")
                        .then(literal("reload")
                                .executes(c -> {
                                    JulyMain.inst.reloadConfig();

                                    c.getSource().sendAdmin("Reloading event config");
                                    return 0;
                                })
                        )

                        .then(literal("save")
                                .executes(c -> {
                                    JulyMain.inst.saveConfig();

                                    c.getSource().sendAdmin("Saving event config");
                                    return 0;
                                })
                        )
                )

                .then(literal("update_lb")
                        .executes(c -> {
                            JulyMain.updateLb();

                            c.getSource().sendAdmin(Component.text("Updating Leaderboard"));
                            return 0;
                        })
                )

                .then(literal("give_ticket")
                        .executes(c -> {
                            Player player = c.getSource().asPlayer();

                            player.getInventory().addItem(EventItems.ticket());

                            c.getSource().sendAdmin("Giving ticket");
                            return 0;
                        })

                        .then(argument("target", EntityArgument.player())
                                .executes(c -> {
                                    Player player = getPlayer(c);

                                    if(UserManager.inst().isAlt(player.getUniqueId())){
                                        player.sendMessage(Component.text("Alt accounts cannot earn tickets").color(NamedTextColor.GRAY));
                                        return 0;
                                    }

                                    player.getInventory().addItem(EventItems.ticket());

                                    c.getSource().sendAdmin("Giving ticket");
                                    return 0;
                                })
                        )
                )
                .then(literal("give_items")
                        .executes(c -> {
                            Player player = c.getSource().asPlayer();
                            EventItems.giveStarter(player);

                            c.getSource().sendAdmin("Giving items");
                            return 0;
                        })
                );
    }

    private Player getPlayer(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return c.getArgument("target", EntitySelector.class).getPlayer(c.getSource());
    }

    private int start(Player player, boolean practise){
        ParkourEvent event = JulyMain.event;

        if(practise) event.startPractise(player);
        else event.start(player);

        return 0;
    }
}