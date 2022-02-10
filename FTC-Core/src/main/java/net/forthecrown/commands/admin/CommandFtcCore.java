package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcConfig;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;

import static net.forthecrown.core.Crown.*;

public class CommandFtcCore extends FtcCommand {

    public CommandFtcCore(){
        super("ftccore", inst());

        setDescription("The primary FTC-Core command");
        setPermission(Permissions.ADMIN);

        register();
    }

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
                .then(literal("maxPlayers")
                        .then(argument("limit", IntegerArgumentType.integer(1))
                                .executes(c -> {
                                    int max = c.getArgument("limit", Integer.class);

                                    Bukkit.setMaxPlayers(max);

                                    c.getSource().sendAdmin("Set max player limit to " + max);
                                    return 0;
                                })
                        )
                )

                .then(literal("clear_attribute_modifiers")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);

                            ItemStack item = user.getInventory().getItemInMainHand();

                            if (ItemStacks.isEmpty(item)) {
                                throw FtcExceptionProvider.mustHoldItem();
                            }

                            ItemMeta meta = item.getItemMeta();

                            if(meta.hasAttributeModifiers()) {
                                for (Map.Entry<Attribute, AttributeModifier> e: meta.getAttributeModifiers().entries()) {
                                    meta.removeAttributeModifier(e.getKey(), e.getValue());
                                }
                            } else {
                                throw FtcExceptionProvider.create("Item has no attribute modifiers");
                            }

                            item.setItemMeta(meta);

                            c.getSource().sendAdmin("Removed all attribute modifiers from held item");
                            return 0;
                        })
                )

                .then(literal("illegal_worlds")
                        .executes(c -> {
                            String joined = ListUtils.join(Crown.config().getIllegalWorlds(), WorldInfo::getName);

                            c.getSource().sendMessage("Illegal worlds: " + joined);
                            return 0;
                        })

                        .then(literal("add")
                                .then(argument("world", WorldArgument.world())
                                        .executes(c -> {
                                            World world = WorldArgument.getWorld(c, "world");
                                            FtcConfig config = Crown.config();

                                            if(config.isIllegalWorld(world)) {
                                                throw FtcExceptionProvider.create(world.getName() + " is already marked as illegal");
                                            }

                                            config.addIllegalWorld(world);

                                            c.getSource().sendAdmin("Added illegal world: " + world.getName());
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("remove")
                                .then(argument("world", WorldArgument.world())
                                        .executes(c -> {
                                            World world = WorldArgument.getWorld(c, "world");
                                            FtcConfig config = Crown.config();

                                            if(!config.isIllegalWorld(world)) {
                                                throw FtcExceptionProvider.create(world.getName() + " is already a legal world");
                                            }

                                            config.removeIllegalWorld(world);

                                            c.getSource().sendAdmin("Removed illegal world: " + world.getName());
                                            return 0;
                                        })
                                )
                        )
                )

                .then(CommandLore.compOrStringArg(literal("tablist_score"),
                        (c, b) -> CompletionProvider.suggestMatching(b,"Deaths", "Crown Score"),
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

                            user.createTeleport(Crown.config()::getServerSpawn, true, true, UserTeleport.Type.TELEPORT)
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

                        .then(literal("should_show")
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

                        .then(literal("should_show_end")
                                .executes(c -> {
                                    c.getSource().sendMessage(
                                            Component.text("Should show join end info: ")
                                                    .append(Component.text(getJoinInfo().shouldShow()))
                                    );
                                    return 0;
                                })

                                .then(argument("shouldShow", BoolArgumentType.bool())
                                        .executes(c -> {
                                            boolean bool = c.getArgument("shouldShow", Boolean.class);
                                            getJoinInfo().setShouldShowEnd(bool);

                                            c.getSource().sendAdmin(
                                                    Component.text("Set should show join end message: ")
                                                            .append(Component.text(bool))
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("set")
                                .then(argument("component", ChatArgument.chat())
                                        .executes(c -> {
                                            Component component = c.getArgument("component", Component.class);
                                            getJoinInfo().setInfo(component);

                                            c.getSource().sendMessage(
                                                    Component.text("Set join info to ")
                                                            .append(component)
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("set_end")
                                .then(argument("component", ChatArgument.chat())
                                        .executes(c -> {
                                            Component component = c.getArgument("component", Component.class);
                                            getJoinInfo().setEndInfo(component);

                                            c.getSource().sendMessage(
                                                    Component.text("Set join end info to ")
                                                            .append(component)
                                            );
                                            return 0;
                                        })
                                )
                        )
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
            if(elite) player.getInventory().addItem(ItemStacks.eliteVoteTicket());
            else player.getInventory().addItem(ItemStacks.voteTicket());
        } catch (Exception e){
            player.sendMessage("Inventory full");
            return 0;
        }
        source.sendAdmin("Giving vote ticket");
        return 0;
    }

    private int giveCoins(Player player, int amount){
        player.getInventory().addItem(ItemStacks.makeCoins(amount, 1));
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
        STRUCTURES ("Structures",               getStructureManager()::save ,   getStructureManager()::reload),
        COMVARS ("ComVars",                     ComVars::save,                  ComVars::reload),
        REGIONS ("Regions",                     getRegionManager()::save,       getRegionManager()::reload),
        ITEM_PRICES ("Item Prices",             getPriceMap()::save,            getPriceMap()::reload),
        MARKETS("Markets",                      getMarkets()::save,             getMarkets()::reload),
        MESSAGES ("Messages",                   () -> {},                       getMessages()::reload),
        PUNISHMENTS("Punishments",              getPunishments()::save,         getPunishments()::reload),
        KITS("Kits",                            getKitManager()::save,          getKitManager()::reload),
        WARPS("Warps",                          getWarpManager()::save,         getWarpManager()::reload),
        INTERACTABLES("Interactable Manager",   getUsables()::saveAll,          getUsables()::reloadAll),
        ANNOUNCER ("Announcer",                 getAnnouncer()::save,           getAnnouncer()::reload),
        BALANCES ("Balances",                   getEconomy()::save,             getEconomy()::reload),
        USERS ("Users",                         getUserManager()::saveUsers,    getUserManager()::reloadUsers),
        SHOPS ("Signshops",                     getShopManager()::save,         getShopManager()::reload),
        CONFIG ("Main Config",                  config()::save,                 config()::reload),
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