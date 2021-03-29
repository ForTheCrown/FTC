package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.core.CrownWeapons;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.exceptions.InvalidPlayerArgumentException;
import net.forthecrown.core.commands.brigadier.types.ParticleType;
import net.forthecrown.core.commands.brigadier.types.TypeCreator;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.files.CrownSignShop;
import net.forthecrown.core.files.FtcUser;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandCore extends CrownCommandBuilder {

    public CommandCore(){
        super("ftccore", FtcCore.getInstance());

        setDescription("The primary FTC-Core command");
        setPermission("ftc.commands.admin");

        this.bals = FtcCore.getBalances();
        this.maxMoney = FtcCore.getMaxMoneyAmount();

        register();
    }

    private static final String USER_ARG = "user";
    private final Balances bals;
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
     * Main Author: Botul
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("resetcrown")
                        .executes(c -> {
                            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                            Objective objective = scoreboard.getObjective("crown");
                            objective.unregister();

                            scoreboard.registerNewObjective("crown", "dummy", Component.text("crown"));

                            broadcastAdmin(c.getSource(), "Crown objective reset");
                            return 0;
                        })
                )
                .then(argument("save")
                        .executes(c -> saveOrReloadAll(getSender(c), true))

                        .then(argument("announcer").executes(c -> saveOrReload(getSender(c), SaveReloadPart.ANNOUNCER, true)))
                        .then(argument("blackmarket").executes(c -> saveOrReload(getSender(c), SaveReloadPart.BLACK_MARKET, true)))
                        .then(argument("balances").executes(c -> saveOrReload(getSender(c), SaveReloadPart.BALANCES, true)))
                        .then(argument("config").executes(c -> saveOrReload(getSender(c), SaveReloadPart.CONFIG, true)))
                        .then(argument("users").executes(c -> saveOrReload(getSender(c), SaveReloadPart.USERS, true)))
                        .then(argument("shops").executes(c -> saveOrReload(getSender(c), SaveReloadPart.SHOPS, true)))
                )
                .then(argument("reloadconfirm")
                        .executes(c -> saveOrReloadAll(getSender(c), false))

                        .then(argument("announcer").executes(c -> saveOrReload(getSender(c), SaveReloadPart.ANNOUNCER, false)))
                        .then(argument("blackmarket").executes(c -> saveOrReload(getSender(c), SaveReloadPart.BLACK_MARKET, false)))
                        .then(argument("balances").executes(c -> saveOrReload(getSender(c), SaveReloadPart.BALANCES, false)))
                        .then(argument("config").executes(c -> saveOrReload(getSender(c), SaveReloadPart.CONFIG, false)))
                        .then(argument("users").executes(c -> saveOrReload(getSender(c), SaveReloadPart.USERS, false)))
                        .then(argument("shops").executes(c -> saveOrReload(getSender(c), SaveReloadPart.SHOPS, false)))
                )
                .then(argument("reload")
                    .executes(c -> {
                        CommandSender sender = getSender(c);
                        sender.sendMessage(ChatColor.RED + "Warning! " + ChatColor.RESET + "You're about to reload one or all of the plugin's configs! Make sure you've saved!");
                        sender.sendMessage("Do /ftccore save to save");
                        sender.sendMessage("Do /ftccore reloadconfirm to confirm");
                        return 0;
                    })
                )
                .then(argument("legacySwordUpdate")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            if(player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR){
                                throw new CrownCommandException("&7You must be holding the weapon to update");
                            }
                            CrownWeapons.updateFromLegacy(player.getInventory().getItemInMainHand());
                            broadcastAdmin(c.getSource(), "Updated Royal sword");
                            return 0;
                        })
                )

                .then(argument("user")
                        .then(argument(USER_ARG, UserType.user())
                                .suggests((c, b) -> UserType.listSuggestions(b))

                                .then(argument("save")
                                        .executes(c -> {
                                            CrownUser u = getUser(c);
                                            u.save();
                                            broadcastAdmin(c.getSource(), "Saved data of " + u.getName());
                                            return 0;
                                        })
                                )
                                .then(argument("reload")
                                        .executes(c -> {
                                            CrownUser u = getUser(c);
                                            u.reload();
                                            broadcastAdmin(c.getSource(), "Reloaded data of " + u.getName());
                                            return 0;
                                        })
                                )

                                .then(argument("balance")
                                        .executes(c-> {
                                            CrownUser user = getUser(c);
                                            c.getSource().getBukkitSender().sendMessage(user.getName() + " has " + bals.getDecimalized(user.getBase()) + " Rhines");
                                            return 0;
                                        })

                                        .then(argument("set")
                                                .then(argument("sAmount", IntegerArgumentType.integer(0, maxMoney))
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);

                                                            int amount = c.getArgument("sAmount", Integer.class);
                                                            bals.set(user.getBase(), amount);

                                                            broadcastAdmin(c.getSource(), "Set " + user.getName() + "'s balance to " + bals.getDecimalized(user.getBase()));
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(argument("add")
                                                .then(argument("aAmount", IntegerArgumentType.integer(1, maxMoney))
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);

                                                            int amount = c.getArgument("aAmount", Integer.class);
                                                            bals.add(user.getBase(), amount, false);

                                                            broadcastAdmin(c.getSource(), "Added " + amount + " to " + user.getName() + "'s balance.");
                                                            broadcastAdmin(c.getSource(), "Now has " + bals.getDecimalized(user.getBase()));
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(argument("remove")
                                                .then(argument("rAmount", IntegerArgumentType.integer(1, maxMoney))
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);

                                                            int amount = c.getArgument("rAmount", Integer.class);
                                                            bals.add(user.getBase(), -amount, false);

                                                            broadcastAdmin(c.getSource(), "Removed " + amount + " from " + user.getName() + "'s balance.");
                                                            broadcastAdmin(c.getSource(), "Now has " + bals.getDecimalized(user.getBase()));
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(argument("reset")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);

                                                    Balances bals = FtcCore.getBalances();
                                                    Map<UUID, Integer> balMap = bals.getBalanceMap();

                                                    balMap.remove(user.getBase());
                                                    bals.setBalanceMap(balMap);

                                                    broadcastAdmin(c.getSource(), "Reset balance of " + user.getName());
                                                    return 0;
                                                })
                                        )
                                )

                                .then(argument("arrowparticle")
                                        .executes(c -> {
                                            CrownUser u = getUser(c);
                                            c.getSource().getBukkitSender().sendMessage(u.getName() + "'s ArrowParticles as a List.toString cuz I'm lazy");
                                            c.getSource().getBukkitSender().sendMessage(u.getParticleArrowAvailable().toString());
                                            return 0;
                                        })

                                        .then(argument("unset")
                                                .executes(c -> {
                                                    CrownUser u = getUser(c);
                                                    u.setArrowParticle(null);
                                                    broadcastAdmin(c.getSource(), "unset");
                                                    return 0;
                                                })
                                        )
                                        .then(argument("set")
                                                .then(argument("activeParticle", ParticleType.particle())
                                                        .executes(c -> {
                                                            CrownUser u = getUser(c);
                                                            Particle p = ParticleType.getParticle(c, "activeParticle");

                                                            u.setArrowParticle(p);
                                                            broadcastAdmin(c.getSource(), "Set " + p.toString() + " as " + u.getName() + "'s active ArrowParticle");;
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(argument("add")
                                                .then(argument("arrowParticle", ParticleType.particle())
                                                        .executes(c -> {
                                                            CrownUser u = getUser(c);
                                                            Particle particle = ParticleType.getParticle(c, "arrowParticle");

                                                            List<Particle> partList = u.getParticleArrowAvailable();
                                                            partList.add(particle);
                                                            u.setParticleArrowAvailable(partList);

                                                            broadcastAdmin(c.getSource(), "Added " + particle.toString() + " to " + u.getName() + "'s Arrow Particles");
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(argument("remove")
                                                .then(argument("rArrowParticle", ParticleType.particle())
                                                        .executes(c -> {
                                                            CrownUser u = getUser(c);
                                                            Particle particle = ParticleType.getParticle(c, "rArrowParticle");

                                                            List<Particle> partList = u.getParticleArrowAvailable();
                                                            partList.remove(particle);
                                                            u.setParticleArrowAvailable(partList);

                                                            broadcastAdmin(c.getSource(), "Removed " + particle.toString() + " from " + u.getName() + "'s Arrow Particles");
                                                            return 0;
                                                        })
                                                )
                                        )
                                )

                                .then(argument("deathparticle")
                                        .executes(c -> {
                                            CrownUser u = getUser(c);
                                            c.getSource().getBukkitSender().sendMessage(u.getName() + "'s DeathParticles as a List.toString cuz I'm lazy");
                                            c.getSource().getBukkitSender().sendMessage(u.getParticleDeathAvailable().toString());
                                            return 0;
                                        })

                                        .then(argument("add")
                                                .then(argument("particle", StringArgumentType.word())
                                                        .executes(c -> {
                                                            CrownUser u = getUser(c);
                                                            String toAdd = c.getArgument("particle", String.class);

                                                            List<String> list = u.getParticleDeathAvailable();
                                                            list.add(toAdd);
                                                            u.setParticleDeathAvailable(list);

                                                            broadcastAdmin(c.getSource(), "Added " + toAdd + " to " + u.getName() + "'s Death Particles");
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(argument("remove")
                                                .then(argument("rParticle", StringArgumentType.word())
                                                        .executes(c -> {
                                                            CrownUser u = getUser(c);
                                                            String toRemove = c.getArgument("rParticle", String.class);

                                                            List<String> list = u.getParticleDeathAvailable();
                                                            list.remove(toRemove);
                                                            u.setParticleDeathAvailable(list);

                                                            broadcastAdmin(c.getSource(), "Removed " + toRemove + " from " + u.getName() + "'s Death Particles");
                                                            return 0;
                                                        })
                                                )
                                        )
                                )

                                .then(argument("baron")
                                        .then(argument("isBaron", BoolArgumentType.bool())
                                                .suggests((c, b) -> suggestMatching(b, "true", "false"))

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

                                                    broadcastAdmin(c.getSource(), user.getName() + " now has pet " + c.getArgument("pet", String.class));
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

                                        .then(argument("list")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);
                                                    c.getSource().getBukkitSender().sendMessage(user.getName() + "'s ranks as a List.toString lol:");
                                                    c.getSource().getBukkitSender().sendMessage(user.getAvailableRanks().toString());
                                                    return 0;
                                                })
                                        )
                                        .then(argument("add")
                                                .then(argument("rankToAdd", StringArgumentType.word())
                                                        .suggests(TypeCreator::listRankSuggestions)
                                                        .executes(c ->{
                                                            CrownUser user = getUser(c);
                                                            Rank rank = TypeCreator.getRank(c, "rankToAdd");

                                                            user.addRank(rank);
                                                            broadcastAdmin(c.getSource(), user.getName() + " now has " + rank.getPrefix());
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
                                                            broadcastAdmin(c.getSource(), user.getName() + " no longer has " + rank.getPrefix());
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
                                                .suggests(TypeCreator::listBranchSuggestions)
                                                .executes(c ->{
                                                    CrownUser user = getUser(c);
                                                    Branch branch = TypeCreator.getBranch(c, "branchToSet");
                                                    user.setBranch(branch);
                                                    broadcastAdmin(c.getSource(), user.getName() + " is now a " + user.getBranch().getSingularName());
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
                                                    broadcastAdmin(c.getSource(), user.getName() + " now has " + user.getGems() + " gems");

                                                    return 0;
                                                })
                                        )
                                )
                                .then(argument("resetearnings")
                                        .executes(c ->{
                                            CrownUser u = getUser(c);
                                            u.resetEarnings();
                                            broadcastAdmin(c.getSource(), u.getName() + " earnings reset");
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
                                            user.unload();
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
                                    Player player = getPlayerSender(c);
                                    player.getInventory().addItem(CrownItems.BASE_ROYAL_SWORD);
                                    broadcastAdmin(c.getSource(), "Giving Royal Sword");
                                    return 0;
                                })
                        )
                        .then(argument("cutlass")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    player.getInventory().addItem(CrownItems.BASE_CUTLASS);
                                    broadcastAdmin(c.getSource(), "Giving Captain's Cutlass");
                                    return 0;
                                })
                        )
                        .then(argument("vikingaxe")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    player.getInventory().addItem(CrownItems.BASE_VIKING_AXE);
                                    broadcastAdmin(c.getSource(), "Giving Viking Axe");
                                    return 0;
                                })
                        )
                        .then(argument("voteticket")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    return giveTicket(false, player);
                                })
                        )
                        .then(argument("eliteticket")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    return giveTicket(true, player);
                                })
                        )
                );
    }

    private int giveTicket(boolean elite, Player player){
        try {
            if(elite) player.getInventory().addItem(CrownItems.ELITE_VOTE_TICKET);
            else player.getInventory().addItem(CrownItems.VOTE_TICKET);
        } catch (Exception e){
            player.sendMessage("Inventory full");
            return 0;
        }
        broadcastAdmin(player, "Giving vote ticket");
        return 0;
    }

    private int giveCoins(Player player, int amount){
        player.getInventory().addItem(CrownItems.getCoins(amount));
        player.sendMessage("You got " + amount + " Rhines worth of coins");
        return 0;
    }

    private int announcerThing(CommandContext<CommandListenerWrapper> c, boolean start){
        if(start){
            FtcCore.getAnnouncer().startAnnouncer();
            broadcastAdmin(c.getSource(), "Announcer started");
            return 0;
        }

        FtcCore.getAnnouncer().stopAnnouncer();
        broadcastAdmin(c.getSource(), "Announcer stopped");
        return 0;
    }

    private CrownUser getUser(CommandContext<CommandListenerWrapper> c) throws InvalidPlayerArgumentException {
        String playerName = c.getArgument(USER_ARG, String.class);
        UUID id = getUUID(playerName);
        return FtcCore.getUser(id);
    }

    private int saveOrReload(CommandSender sender, SaveReloadPart thingTo, boolean save){
        thingTo.run(save);
        if(save) broadcastAdmin(sender, thingTo.getSaveMessage());
        else broadcastAdmin(sender, thingTo.getReloadMessage());
        return 0;
    }

    private int saveOrReloadAll(CommandSender sender, boolean save){
        for (SaveReloadPart t: SaveReloadPart.values()){
            t.run(save);
            if(save) sender.sendMessage(t.getSaveMessage());
            else sender.sendMessage(t.getReloadMessage());
        }

        if(save) broadcastAdmin(sender, "Saved FTC-Core");
        else broadcastAdmin(sender, "Reloaded FTC-Core");
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
            if(b) for (FtcUser u: FtcCore.LOADED_USERS.values()) u.save();
            else for (FtcUser u: FtcCore.LOADED_USERS.values()) u.reload();
        }),
        SHOPS ("Signshops", b ->{
            if(b) for (CrownSignShop u: FtcCore.LOADED_SHOPS.values()){
                try {
                    u.save();
                } catch (Exception ignored) {}
            }
            else for (CrownSignShop u: FtcCore.LOADED_SHOPS.values()){
                try {
                    u.reload();
                } catch (Exception ignored) {}
            }
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
