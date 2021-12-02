package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.forthecrown.commands.arguments.UserParseResult;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.marriage.CommandMarry;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class CommandFtcCore extends FtcCommand {

    public CommandFtcCore(){
        super("ftccore", Crown.inst());

        setDescription("The primary FTC-Core command");
        setPermission(Permissions.FTC_ADMIN);

        this.bals = Crown.getEconomy();
        this.maxMoney = ComVars.getMaxMoneyAmount();

        register();
    }

    private static final String USER_ARG = "user";
    private final Economy bals;
    private final int maxMoney;

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Used by admins for general utility purposes
     *
     * Valid usages of command:
     * - too many to list lol
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(CommandLore.compOrStringArg(literal("tablist_score"),
                        (c, b) -> CompletionProvider.suggestMatching(b,"Deaths", " Crown Score"),
                        (c, field) -> {
                            Crown.getTabList().setScore(field);
                            Crown.getTabList().updateList();

                            c.getSource().sendAdmin(
                                    Component.text("Set tab score field to ")
                                            .append(field)
                            );
                            return 0;
                        })
                )

                .then(literal("updateDate")
                        .executes(c -> {
                            Crown.getDayUpdate().update();

                            c.getSource().sendAdmin("Updated dated");
                            return 0;
                        })
                )

                .then(literal("resetcrown") //Resets the crown objective, aka, destroys and re creates it
                        .executes(c -> {
                            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                            Objective objective = scoreboard.getObjective("crown");
                            objective.unregister();

                            scoreboard.registerNewObjective("crown", "dummy", Component.text("crown"));

                            c.getSource().sendAdmin( "Crown objective reset");
                            return 0;
                        })
                )

                .then(literal("goto_spawn")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);

                            user.createTeleport(Crown::getServerSpawn, true, true, UserTeleport.Type.TELEPORT)
                                    .start(true);

                            user.sendMessage(Component.text("Going to spawn").color(NamedTextColor.GRAY));
                            return 0;
                        })
                )

                .then(literal("join_info")
                        .then(literal("display")
                                .executes(c -> {
                                    c.getSource().sendMessage(Crown.getJoinInfo().display());
                                    return 0;
                                })
                        )

                        .then(literal("show")
                                .executes(c -> {
                                    c.getSource().sendMessage(
                                            Component.text("Should show join info: ")
                                                    .append(Component.text(Crown.getJoinInfo().shouldShow()))
                                    );
                                    return 0;
                                })

                                .then(argument("shouldShow", BoolArgumentType.bool())
                                        .executes(c -> {
                                            boolean bool = c.getArgument("shouldShow", Boolean.class);
                                            Crown.getJoinInfo().setShouldShow(bool);

                                            c.getSource().sendAdmin(
                                                    Component.text("Set should show join message: ")
                                                            .append(Component.text(bool))
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("set")
                                .then(argument("component", ComponentArgument.component())
                                        .executes(c -> {
                                            Component component = c.getArgument("component", Component.class);
                                            Crown.getJoinInfo().setDisplay(component);

                                            c.getSource().sendMessage(
                                                    Component.text("Set join info to ")
                                                            .append(component)
                                            );
                                            return 0;
                                        })
                                )
                        )
                )

                .then(literal("marriagePriest")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            Location location = user.getLocation();

                            location.getWorld().spawn(location, Villager.class, villie -> {
                                villie.setProfession(Villager.Profession.CLERIC);
                                villie.setVillagerType(Villager.Type.PLAINS);
                                villie.setVillagerLevel(5);

                                villie.setInvulnerable(true);
                                villie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);

                                villie.customName(Component.text("Father Ted").color(NamedTextColor.AQUA));
                                villie.setCustomNameVisible(true);

                                villie.getPersistentDataContainer().set(CommandMarry.KEY, PersistentDataType.BYTE, (byte) 1);
                            });

                            c.getSource().sendAdmin(Component.text("Spawned marriage priest"));
                            return 0;
                        })
                )

                .then(saveReloadArgEffortless(true))
                .then(saveReloadArgEffortless(false))

                .then(literal("item")
                        .then(literal("coin")
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(c -> giveCoins(getPlayerSender(c), c.getArgument("amount", Integer.class)))
                                )
                                .executes(c -> giveCoins(getPlayerSender(c), 100))
                        )

                        .then(literal("voteticket").executes(c -> giveTicket(false, getPlayerSender(c), c.getSource())))
                        .then(literal("eliteticket").executes(c -> giveTicket(true, getPlayerSender(c), c.getSource())))
                );
    }

    private LiteralArgumentBuilder<CommandSource> saveReloadArgEffortless(boolean save){
        return literal(save ? "save" : "reload")
                .executes(c -> saveOrReloadAll(c.getSource(), save))

                .then(argument("part", EnumArgument.of(SaveReloadPart.class))
                        .executes(c -> saveOrReload(
                                c.getSource(),
                                c.getArgument("part", SaveReloadPart.class),
                                save
                        ))
                );
    }

    private int giveTicket(boolean elite, Player player, CommandSource source){
        try {
            if(elite) player.getInventory().addItem(FtcItems.eliteVoteTicket());
            else player.getInventory().addItem(FtcItems.voteTicket());
        } catch (Exception e){
            player.sendMessage("Inventory full");
            return 0;
        }
        source.sendAdmin("Giving vote ticket");
        return 0;
    }

    private int giveCoins(Player player, int amount){
        player.getInventory().addItem(FtcItems.makeCoins(amount, 1));
        player.sendMessage("You got " + amount + " Rhines worth of coins");
        return 0;
    }

    private int announcerThing(CommandContext<CommandSource> c, boolean start){
        if(start){
            Crown.getAnnouncer().start();
            c.getSource().sendAdmin( "Announcer started");
            return 0;
        }

        Crown.getAnnouncer().stop();
        c.getSource().sendAdmin( "Announcer stopped");
        return 0;
    }

    private CrownUser getUser(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return c.getArgument(USER_ARG, UserParseResult.class).getUser(c.getSource(), false);
    }

    private int saveOrReload(CommandSource sender, SaveReloadPart thingTo, boolean save){
        thingTo.function.accept(save);
        if(save) sender.sendAdmin(thingTo.saveMessage());
        else sender.sendAdmin(thingTo.reloadMessage());
        return 0;
    }

    private int saveOrReloadAll(CommandSource sender, boolean save){
        for (SaveReloadPart t: SaveReloadPart.values()){
            t.function.accept(save);
            if(save) sender.sendMessage(t.saveMessage());
            else sender.sendMessage(t.reloadMessage());
        }

        if(save) sender.sendAdmin( "Saved FTC-Core");
        else sender.sendAdmin("Reloaded FTC-Core");
        return 0;
    }

    private enum SaveReloadPart {
        COMVARS ("ComVars", b -> {
           if(b) ComVars.reload();
           else ComVars.save();
        }),
        REGIONS ("Regions", b -> {
            if(b) Crown.getRegionManager().reload();
            else Crown.getRegionManager().save();
        }),
        ITEM_PRICES ("Item Prices", b -> {
            if(b) Crown.getPriceMap().reload();
            else Crown.getPriceMap().save();
        }),
        MARKTES("Markets", b -> {
           if(b) Crown.getMarkets().reload();
           else Crown.getMarkets().save();
        }),
        JOIN_INFO ("Join info", b -> {
            if(!b) Crown.getJoinInfo().reload();
            else Crown.getJoinInfo().save();
        }),
        MESSAGES ("Messages", b -> {
            if(!b) Crown.getMessages().reload();
        }),
        RULES ("Rules", b -> {
            if(b) Crown.getRules().save();
            else Crown.getRules().reload();
        }),
        KING ("Kingship", b -> {
           if(b) Crown.getKingship().save();
           else Crown.getKingship().reload();
        }),
        PUNISHMENTS("Punishments", b ->{
            if(b) Crown.getPunishmentManager().save();
            else Crown.getPunishmentManager().reload();
        }),
        KITS("Kits", b -> {
            if(b) Crown.getKitManager().save();
            else Crown.getKitManager().reload();
        }),
        WARPS("Warps", b -> {
            if(b) Crown.getWarpManager().save();
            else Crown.getWarpManager().reload();
        }),
        INTERACTABLES("Interactable Manager", b -> {
           if(b) Crown.getUsablesManager().saveAll();
           else Crown.getUsablesManager().reloadAll();
        }),
        ANNOUNCER ("Announcer", (b) -> {
            if(b) Crown.getAnnouncer().save();
            else Crown.getAnnouncer().reload();
        }),
        BALANCES ("Balances", b -> {
            if(b) Crown.getEconomy().save();
            else Crown.getEconomy().reload();
        }),
        USERS ("Users", b -> {
            if(b) UserManager.inst().saveUsers();
            else UserManager.inst().reloadUsers();
        }),
        SHOPS ("Signshops", b ->{
            if(b) Crown.getShopManager().save();
            else Crown.getShopManager().reload();
        }),
        CONFIG ("Main Config", b -> {
            if(b) Crown.inst().saveConfig();
            else Crown.inst().reloadConfig();
        }),
        USER_MANAGER("User Manager", b ->{
            if(b) UserManager.inst().save();
            else UserManager.inst().reload();
        });

        private final String msg;
        private final BooleanConsumer function;
        SaveReloadPart(String msg, BooleanConsumer runnable){
            this.msg = msg;
            this.function = runnable;
        }

        public String reloadMessage() {
            return FtcFormatter.formatColorCodes("&7" + msg + " reloaded.");
        }

        public String saveMessage() {
            return FtcFormatter.formatColorCodes("&7" + msg + " saved.");
        }
    }
}