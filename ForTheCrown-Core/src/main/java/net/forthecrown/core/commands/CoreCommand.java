package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.core.CrownItems;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.TypeCreator;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.exceptions.InvalidPlayerArgumentException;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.files.CrownSignShop;
import net.forthecrown.core.files.FtcUser;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class CoreCommand extends CrownCommandBuilder {

    public CoreCommand(){
        super("ftccore", FtcCore.getInstance());

        setDescription("The primary FTC-Core command");
        setPermission("ftc.commands.admin");
        setUsage("&7Usage:&r /ftcore <reload | save | crownitem | announcer | reload>");

        register();
    }

    private static final String USER_ARG = "user";

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .then(argument("save")
                        .executes(c -> doAllThings(getSender(c), true))

                        .then(argument("announcer").executes(c -> doThing(getSender(c), SaveReloadPart.ANNOUNCER, true)))
                        .then(argument("blackmarket").executes(c -> doThing(getSender(c), SaveReloadPart.BLACK_MARKET, true)))
                        .then(argument("balances").executes(c -> doThing(getSender(c), SaveReloadPart.BALANCES, true)))
                        .then(argument("config").executes(c -> doThing(getSender(c), SaveReloadPart.CONFIG, true)))
                        .then(argument("users").executes(c -> doThing(getSender(c), SaveReloadPart.USERS, true)))
                        .then(argument("shops").executes(c -> doThing(getSender(c), SaveReloadPart.SHOPS, true)))
                )
                .then(argument("reloadconfirm")
                        .executes(c -> doAllThings(getSender(c), false))

                        .then(argument("announcer").executes(c -> doThing(getSender(c), SaveReloadPart.ANNOUNCER, false)))
                        .then(argument("blackmarket").executes(c -> doThing(getSender(c), SaveReloadPart.BLACK_MARKET, false)))
                        .then(argument("balances").executes(c -> doThing(getSender(c), SaveReloadPart.BALANCES, false)))
                        .then(argument("config").executes(c -> doThing(getSender(c), SaveReloadPart.CONFIG, false)))
                        .then(argument("users").executes(c -> doThing(getSender(c), SaveReloadPart.USERS, false)))
                        .then(argument("shops").executes(c -> doThing(getSender(c), SaveReloadPart.SHOPS, false)))
                )
                .then(argument("reload")
                    .executes(c -> {
                        CommandSender sender = getSender(c);
                        sender.sendMessage(ChatColor.RED + "Warning! " + ChatColor.RESET + "You're about to reload one or all of the plugin's configs! Make sure you've saved!");
                        sender.sendMessage("Do /ftccore reloadconfirm to confirm");
                        return 0;
                    })
                )
                .then(argument("user")
                        .then(argument(USER_ARG, StringArgumentType.word())
                                .suggests((c, b) -> getPlayerList(b).buildFuture())

                                .then(argument("baron")
                                        .then(argument("isBaron", BoolArgumentType.bool())
                                                .suggests((c, b) -> listCompletions(b, "true", "false").buildFuture())

                                                .executes(c ->{
                                                    CrownUser user = getUser(c);
                                                    boolean isBaron = c.getArgument("isBaron", Boolean.class);
                                                    if(user.isBaron() == isBaron) throw new CrownCommandException(user.getName() + "'s baron value is the same as entered!");

                                                    user.setBaron(isBaron);
                                                    getSender(c).sendMessage(user.getName() + " isBaron " + user.isBaron());
                                                    return 0;
                                                })
                                        )
                                        .executes(c ->{
                                            CrownUser user = getUser(c);
                                            getSender(c).sendMessage(user.getName() + " is baron: " + user.isBaron());
                                            return 0;
                                        })
                                )
                                .then(argument("addpet")
                                        .then(argument("pet", StringArgumentType.word())
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);

                                                    List<String> pets = user.getPets();
                                                    pets.add(c.getArgument("pet", String.class));
                                                    user.setPets(pets);

                                                    getSender(c).sendMessage(user.getName() + " pet added");
                                                    return 0;
                                                })
                                        )
                                )
                                .then(argument("rank")
                                        .executes(c -> {
                                            CrownUser user = getUser(c);
                                            getSender(c).sendMessage(user.getName() + "'s rank is " + user.getRank().getPrefix());
                                            return 0;
                                        })

                                        .then(argument("add")
                                                .then(argument("rankToAdd", StringArgumentType.word())
                                                        .suggests(TypeCreator::listRankSuggestions)
                                                        .executes(c ->{
                                                            CrownUser user = getUser(c);
                                                            Rank rank = TypeCreator.getRank(c, "rankToAdd");

                                                            user.addRank(rank);
                                                            getSender(c).sendMessage(user.getName() + " now has " + rank.getPrefix());
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(argument("remove")
                                                .then(argument("rankToRemove", StringArgumentType.word())
                                                        .suggests(TypeCreator::listRankSuggestions)
                                                        .executes(c ->{
                                                            CrownUser user = getUser(c);
                                                            Rank rank = TypeCreator.getRank(c, "rankToRemove");

                                                            user.removeRank(rank);
                                                            c.getSource().getBukkitSender().sendMessage(user.getName() + " no longer has " + rank.getPrefix());
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                                .then(argument("branch")
                                        .executes(c ->{
                                            CrownUser user = getUser(c);
                                            getSender(c).sendMessage(user.getName() + "'s branch is " + user.getBranch().toString());
                                            return 0;
                                        })
                                        .then(argument("branchToSet", StringArgumentType.word())
                                                .suggests(TypeCreator::listCompletionsBranch)
                                                .executes(c ->{
                                                    CrownUser user = getUser(c);
                                                    Branch branch = TypeCreator.getBranch(c, "branchToSet");
                                                    user.setBranch(branch);
                                                    getSender(c).sendMessage(user.getName() + " is now a " + branch.getName());
                                                    return 0;
                                                })
                                        )
                                )
                                .then(argument("addgems")
                                        .then(argument("gemAmount", IntegerArgumentType.integer())
                                                .executes(c ->{
                                                    CrownUser user = getUser(c);
                                                    int gems = c.getArgument("gemAmount", Integer.class);

                                                    user.addGems(gems);
                                                    getSender(c).sendMessage(user.getName() + " now has " + user.getGems() + " gems");
                                                    return 0;
                                                })
                                        )
                                )
                                .then(argument("resetearnings")
                                        .executes(c ->{
                                            getUser(c).resetEarnings();
                                            getSender(c).sendMessage(getUser(c).getName() + " earings' reset");
                                            return 0;
                                        })
                                )
                                .then(argument("totalreset")
                                        .executes(c -> {
                                            c.getSource().getBukkitSender().sendMessage("Uhm nothing lol");
                                            return 0;
                                        })
                                )
                                .then(argument("delete")
                                        .executes(c ->{
                                            CrownUser user = getUser(c);

                                            user.delete();
                                            getSender(c).sendMessage(user.getName() + "'s user data has been deleted");
                                            return 0;
                                        })
                                )
                        )
                )
                .then(argument("announcer")
                        .then(argument("start").executes(c -> announcerThing(c, true)))
                        .then(argument("stop").executes(c -> announcerThing(c, false)))
                )
                .then(argument("item")
                        .then(argument("crown")
                                .then(argument("level", IntegerArgumentType.integer(1, 6))
                                        .then(argument("owner", StringArgumentType.greedyString())
                                                .executes(c ->{
                                                    Player player = getPlayerSender(c);
                                                    int level = c.getArgument("level", Integer.class);
                                                    String owner = c.getArgument("owner", String.class);

                                                    player.getInventory().addItem(CrownItems.getCrown(level, owner));
                                                    getSender(c).sendMessage("You got a level " + level + " crown");

                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(argument("coin")
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(c -> giveCoins(getPlayerSender(c), c.getArgument("amount", Integer.class)))
                                )
                                .executes(c -> giveCoins(getPlayerSender(c), 100))
                        )
                        .then(argument("royalsword")
                                .executes(c -> {
                                    c.getSource().getBukkitSender().sendMessage("Uhm nothing lol");
                                    return 0;
                                })
                        )
                        .then(argument("cutlass")
                                .executes(c -> {
                                    c.getSource().getBukkitSender().sendMessage("Uhm nothing lol");
                                    return 0;
                                })
                        )
                );
    }

    private int giveCoins(Player player, int amount){
        player.getInventory().addItem(CrownItems.getCoins(amount));
        player.sendMessage("You got " + amount + " Rhines worth of coins");
        return 0;
    }

    private int announcerThing(CommandContext<CommandListenerWrapper> c, boolean start){
        if(start){
            FtcCore.getAnnouncer().startAnnouncer();
            getSender(c).sendMessage("Announcer started");
            return 0;
        }

        FtcCore.getAnnouncer().stopAnnouncer();
        getSender(c).sendMessage("Announcer stopped");
        return 0;
    }

    private CrownUser getUser(CommandContext<CommandListenerWrapper> c) throws InvalidPlayerArgumentException {
        String playerName = c.getArgument(USER_ARG, String.class);
        UUID id = getUUID(playerName);
        return FtcCore.getUser(id);
    }

    private int doThing(CommandSender sender, SaveReloadPart thingTo, boolean save){
        thingTo.run(save);
        if(save) sender.sendMessage(thingTo.getSaveMessage());
        else sender.sendMessage(thingTo.getReloadMessage());
        return 0;
    }

    private int doAllThings(CommandSender sender, boolean save){
        for (SaveReloadPart t: SaveReloadPart.values()){
            t.run(save);
            if(save) sender.sendMessage(t.getSaveMessage());
            else sender.sendMessage(t.getReloadMessage());
        }

        Announcer.log(Level.INFO, "FTC-Core saved");
        return 0;
    }

    private enum SaveReloadPart {
        ANNOUNCER ("Announcer", (b) -> {
            if(b) FtcCore.getAnnouncer().save();
            else FtcCore.getAnnouncer().reload();
        }),
        BALANCES ("Balances", b -> {
            if(b) FtcCore.getBalances().save();
            else FtcCore.getBalances().reload();
        }),
        USERS ("Users", b -> {
            if(b) for (FtcUser u: FtcCore.loadedUsers) u.save();
            else for (FtcUser u: FtcCore.loadedUsers) u.reload();
        }),
        SHOPS ("Signshops", b ->{
            if(b) for (CrownSignShop u: FtcCore.loadedShops) u.save();
            else for (CrownSignShop u: FtcCore.loadedShops) u.reload();
        }),
        BLACK_MARKET ("Black Market", b -> {
            if(b) FtcCore.getBlackMarket().save();
            else FtcCore.getBlackMarket().reload();
        }),
        CONFIG ("Main Config", b -> {
            if(b) FtcCore.getInstance().saveConfig();
            else FtcCore.getInstance().reloadConfig();
        });

        private final String msg;
        private final Runnable runnable;
        SaveReloadPart(String msg, Runnable runnable){
            this.msg = msg;
            this.runnable = runnable;
        }

        public String getReloadMessage() {
            return CrownUtils.translateHexCodes("&7" + msg + " reloaded.");
        }

        public String getSaveMessage() {
            return CrownUtils.translateHexCodes("&7" + msg + " saved.");
        }

        public void run(boolean save){
            runnable.run(save);
        }
    }

    private interface Runnable{
        void run(boolean save);
    }
}
