package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserParseResult;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.marriage.CommandMarry;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.UserTeleport;
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

import static net.forthecrown.core.Crown.*;

public class CommandFtcCore extends FtcCommand {

    public CommandFtcCore(){
        super("ftccore", inst());

        setDescription("The primary FTC-Core command");
        setPermission(Permissions.ADMIN);

        register();
    }

    private static final String USER_ARG = "user";

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
                            getTabList().setScore(field);
                            getTabList().updateList();

                            c.getSource().sendAdmin(
                                    Component.text("Set tab score field to ")
                                            .append(field)
                            );
                            return 0;
                        })
                )

                .then(literal("updateDate")
                        .executes(c -> {
                            getDayUpdate().update();

                            c.getSource().sendAdmin("Updated day");
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
                                    c.getSource().sendMessage(getJoinInfo().display());
                                    return 0;
                                })
                        )

                        .then(literal("show")
                                .executes(c -> {
                                    c.getSource().sendMessage(
                                            Component.text("Should show join info: ")
                                                    .append(Component.text(getJoinInfo().shouldShow()))
                                    );
                                    return 0;
                                })

                                .then(argument("shouldShow", BoolArgumentType.bool())
                                        .executes(c -> {
                                            boolean bool = c.getArgument("shouldShow", Boolean.class);
                                            getJoinInfo().setShouldShow(bool);

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
                                            getJoinInfo().setDisplay(component);

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
            getAnnouncer().start();
            c.getSource().sendAdmin( "Announcer started");
            return 0;
        }

        getAnnouncer().stop();
        c.getSource().sendAdmin( "Announcer stopped");
        return 0;
    }

    private CrownUser getUser(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return c.getArgument(USER_ARG, UserParseResult.class).getUser(c.getSource(), false);
    }

    private int saveOrReload(CommandSource sender, SaveReloadPart thingTo, boolean save){
        if(save) {
            sender.sendAdmin(thingTo.saveMessage());
            thingTo.saveFunc.run();
        } else {
            sender.sendAdmin(thingTo.reloadMessage());
            thingTo.loadFunc.run();
        }
        return 0;
    }

    private int saveOrReloadAll(CommandSource sender, boolean save){
        for (SaveReloadPart t: SaveReloadPart.values()){
            if(save) {
                sender.sendMessage(t.saveMessage());
                t.saveFunc.run();
            } else {
                sender.sendMessage(t.reloadMessage());
                t.loadFunc.run();
            }
        }

        if(save) sender.sendAdmin( "Saved FTC-Core");
        else sender.sendAdmin("Reloaded FTC-Core");
        return 0;
    }

    private enum SaveReloadPart {
        COMVARS ("ComVars",                     ComVars::save,                  ComVars::reload),
        REGIONS ("Regions",                     getRegionManager()::save,       getRegionManager()::reload),
        ITEM_PRICES ("Item Prices",             getPriceMap()::save,            getPriceMap()::reload),
        MARKETS("Markets",                      getMarkets()::save,             getMarkets()::reload),
        JOIN_INFO ("Join info",                 getJoinInfo()::save,            getJoinInfo()::save),
        MESSAGES ("Messages",                   () -> {},                       getMessages()::reload),
        RULES ("Rules",                         getRules()::save,               getRules()::reload),
        KING ("Kingship",                       getKingship()::save,            getKingship()::reload),
        PUNISHMENTS("Punishments",              getPunishmentManager()::save,   getPunishmentManager()::reload),
        KITS("Kits",                            getKitManager()::save,          getKitManager()::reload),
        WARPS("Warps",                          getWarpManager()::save,         getWarpManager()::reload),
        INTERACTABLES("Interactable Manager",   getUsables()::saveAll,          getUsables()::reloadAll),
        ANNOUNCER ("Announcer",                 getAnnouncer()::save,           getAnnouncer()::reload),
        BALANCES ("Balances",                   getEconomy()::save,             getEconomy()::reload),
        USERS ("Users",                         getUserManager()::saveUsers,    getUserManager()::reloadUsers),
        SHOPS ("Signshops",                     getShopManager()::save,         getShopManager()::reload),
        CONFIG ("Main Config",                  inst()::saveConfig,             inst()::reloadConfig),
        USER_MANAGER("User Manager",            getUserManager()::save,         getUserManager()::reload);

        private final String msg;
        private final Runnable saveFunc, loadFunc;
        SaveReloadPart(String msg, Runnable save, Runnable load){
            this.msg = msg;
            this.saveFunc = save;
            this.loadFunc = load;
        }

        public String reloadMessage() {
            return FtcFormatter.formatColorCodes("&7" + msg + " reloaded.");
        }

        public String saveMessage() {
            return FtcFormatter.formatColorCodes("&7" + msg + " saved.");
        }
    }
}