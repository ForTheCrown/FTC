package net.forthecrown.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownWeapons;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.*;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.LiteralArgument;
import net.forthecrown.core.commands.brigadier.RequiredArgument;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.ParticleType;
import net.forthecrown.core.commands.brigadier.types.custom.BranchType;
import net.forthecrown.core.commands.brigadier.types.custom.PetType;
import net.forthecrown.core.commands.brigadier.types.custom.RankType;
import net.forthecrown.core.commands.brigadier.types.custom.UserType;
import net.forthecrown.core.datafixers.ShopTagUpdater;
import net.forthecrown.core.datafixers.UserAndBalanceUpdater;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Pet;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.types.user.FtcUserAlt;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.SystemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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
                .then(argument("resetcrown") //Resets the crown objective, aka, destroys and re creates it
                        .executes(c -> {
                            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                            Objective objective = scoreboard.getObjective("crown");
                            objective.unregister();

                            scoreboard.registerNewObjective("crown", "dummy", Component.text("crown"));

                            broadcastAdmin(c.getSource(), "Crown objective reset");
                            return 0;
                        })
                )
                .then(argument("save") //Save all or a part of the plugin
                        .executes(c -> saveOrReloadAll(getSender(c), true)) //Save all

                        //Save a specific part
                        .then(saveReloadArg(SaveReloadPart.ANNOUNCER, true))
                        .then(saveReloadArg(SaveReloadPart.BLACK_MARKET, true))
                        .then(saveReloadArg(SaveReloadPart.BALANCES, true))
                        .then(saveReloadArg(SaveReloadPart.CONFIG, true))
                        .then(saveReloadArg(SaveReloadPart.USERS, true))
                        .then(saveReloadArg(SaveReloadPart.USER_MANAGER, true))
                        .then(saveReloadArg(SaveReloadPart.SHOPS, true))
                )
                .then(argument("reload")
                        .executes(c -> saveOrReloadAll(getSender(c), false)) //Reload all

                        //Reload a specific part
                        .then(saveReloadArg(SaveReloadPart.ANNOUNCER, false))
                        .then(saveReloadArg(SaveReloadPart.BLACK_MARKET, false))
                        .then(saveReloadArg(SaveReloadPart.BALANCES, false))
                        .then(saveReloadArg(SaveReloadPart.CONFIG, false))
                        .then(saveReloadArg(SaveReloadPart.USERS, false))
                        .then(saveReloadArg(SaveReloadPart.USER_MANAGER, false))
                        .then(saveReloadArg(SaveReloadPart.SHOPS, false))
                )

                //Everything relating to a specific user
                .then(argument("user")
                        .then(argument(USER_ARG, UserType.user())
                                .suggests(UserType::suggestSelector)

                                //Save the user's data
                                .then(argument("save")
                                        .executes(c -> {
                                            CrownUser u = getUser(c);
                                            u.save();
                                            broadcastAdmin(c.getSource(), "Saved data of " + u.getName());
                                            return 0;
                                        })
                                )

                                //Reload the user's data
                                .then(argument("reload")
                                        .executes(c -> {
                                            CrownUser u = getUser(c);
                                            u.reload();
                                            broadcastAdmin(c.getSource(), "Reloaded data of " + u.getName());
                                            return 0;
                                        })
                                )

                                //This alt shit can go fuck itself
                                .then(argument("alt")
                                        .then(argument("list")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);
                                                    UserManager um = FtcCore.getUserManager();

                                                    c.getSource().getBukkitSender().sendMessage(
                                                            Component.text(user.getName() + "'s alt accounts:")
                                                            .append(Component.newline())
                                                            .append(Component.text(um.getAlts(user.getUniqueId()).toString()))
                                                    );

                                                    return 0;
                                                })
                                        )
                                        .then(argument("for")
                                                .then(argument("altFor", UserType.user())
                                                        .suggests(UserType::suggest)

                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);
                                                            CrownUser main = UserType.getUser(c, "altFor");
                                                            UserManager um = FtcCore.getUserManager();

                                                            um.addEntry(user.getUniqueId(), main.getUniqueId());
                                                            user.unload();
                                                            CrownUserAlt alt = new FtcUserAlt(user.getUniqueId(), main.getUniqueId());
                                                            alt.save();

                                                            broadcastAdmin(c.getSource(), alt.getName() + " is now an alt for " + main.getName());
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(argument("for_none")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);
                                                    UserManager um = FtcCore.getUserManager();

                                                    if(!um.isAlt(user.getUniqueId())) throw new CrownCommandException(user.getName() + " is not an alt");
                                                    um.removeEntry(user.getUniqueId());

                                                    broadcastAdmin(c.getSource(), user.getName() + " is no longer an alt");
                                                    return 0;
                                                })
                                        )
                                )

                                .then(argument("balance")
                                        .executes(c-> { //Shows the balance
                                            CrownUser user = getUser(c);
                                            c.getSource().getBukkitSender().sendMessage(user.getName() + " has " + bals.getDecimalized(user.getUniqueId()) + " Rhines");
                                            return 0;
                                        })

                                        //Sets the balance
                                        .then(argument("set")
                                                .then(argument("sAmount", IntegerArgumentType.integer(0, maxMoney))
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);

                                                            int amount = c.getArgument("sAmount", Integer.class);
                                                            bals.set(user.getUniqueId(), amount);

                                                            broadcastAdmin(c.getSource(), "Set " + user.getName() + "'s balance to " + bals.getDecimalized(user.getUniqueId()));
                                                            return 0;
                                                        })
                                                )
                                        )
                                        //Adds to the balance
                                        .then(argument("add")
                                                .then(argument("aAmount", IntegerArgumentType.integer(1, maxMoney))
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);

                                                            int amount = c.getArgument("aAmount", Integer.class);
                                                            bals.add(user.getUniqueId(), amount, false);

                                                            broadcastAdmin(c.getSource(), "Added " + amount + " to " + user.getName() + "'s balance.");
                                                            broadcastAdmin(c.getSource(), "Now has " + bals.getDecimalized(user.getUniqueId()));
                                                            return 0;
                                                        })
                                                )
                                        )
                                        //Removes from the balance
                                        .then(argument("remove")
                                                .then(argument("rAmount", IntegerArgumentType.integer(1, maxMoney))
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);

                                                            int amount = c.getArgument("rAmount", Integer.class);
                                                            bals.add(user.getUniqueId(), -amount, false);

                                                            broadcastAdmin(c.getSource(), "Removed " + amount + " from " + user.getName() + "'s balance.");
                                                            broadcastAdmin(c.getSource(), "Now has " + bals.getDecimalized(user.getUniqueId()));
                                                            return 0;
                                                        })
                                                )
                                        )
                                        //Resets the balance, removes it from the BalanceMap so it doesn't take up as much data
                                        .then(argument("reset")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);

                                                    Balances bals = FtcCore.getBalances();
                                                    Map<UUID, Integer> balMap = bals.getBalanceMap();

                                                    balMap.remove(user.getUniqueId());
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
                                .then(argument("pets")
                                        .then(argument("list")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);

                                                    IChatBaseComponent component = new ChatComponentText(user.getName() + "'s pets: " +
                                                            ListUtils.join(user.getPets(), pet -> pet.toString().toLowerCase())
                                                    );
                                                    c.getSource().base.sendMessage(component, SystemUtils.b);
                                                    return 0;
                                                })
                                        )
                                        .then(argument("pet", PetType.pet())
                                                .suggests(PetType::suggest)

                                                .then(argument("add")
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);
                                                            Pet pet = PetType.getPet(c, "pet");

                                                            if(user.hasPet(pet)) throw new CrownCommandException(user.getName() + " already has that pet");

                                                            user.addPet(pet);
                                                            broadcastAdmin(c.getSource(), "Added " + pet.toString() + " to " + user.getName());
                                                            return 0;
                                                        })
                                                )
                                                .then(argument("remove")
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);
                                                            Pet pet = PetType.getPet(c, "pet");

                                                            if(!user.hasPet(pet)) throw new CrownCommandException(user.getName() + " doesn't have that pet");

                                                            user.removePet(pet);
                                                            broadcastAdmin(c.getSource(), "Removed " + pet.toString() + " from " + user.getName());
                                                            return 0;
                                                        })
                                                )
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
                                                .then(argument("rankToAdd", RankType.rank())
                                                        .suggests(RankType::suggest)
                                                        .executes(c ->{
                                                            CrownUser user = getUser(c);
                                                            Rank rank = RankType.getRank(c, "rankToRemove");

                                                            user.addRank(rank);
                                                            broadcastAdmin(c.getSource(), user.getName() + " now has " + rank.getPrefix());
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(argument("remove")
                                                .then(argument("rankToRemove", RankType.rank())
                                                        .suggests(RankType::suggest)
                                                        .executes(c ->{
                                                            CrownUser user = getUser(c);
                                                            Rank rank = RankType.getRank(c, "rankToRemove");

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
                                        .then(argument("branchToSet", BranchType.branch())
                                                .suggests(BranchType::suggest)
                                                .executes(c ->{
                                                    CrownUser user = getUser(c);
                                                    Branch branch = BranchType.getBranch(c, "branchToSet");
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

                .then(argument("datafix")
                        .then(argument("shops")
                                .executes(c -> {
                                    broadcastAdmin(c.getSource(), "Running DataFixer");
                                    try {
                                        new ShopTagUpdater(FtcCore.getInstance()).begin().complete();
                                    } catch (IOException e){
                                        e.printStackTrace();
                                    }

                                    broadcastAdmin(c.getSource(), "Datafixer complete");
                                    return 0;
                                })
                        )
                        .then(argument("users")
                                .executes(c -> {
                                    broadcastAdmin(c.getSource(), "Running data fixer");

                                    try {
                                        new UserAndBalanceUpdater(FtcCore.getInstance()).begin().complete();
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }

                                    broadcastAdmin(c.getSource(), "Datafixer complete");
                                    return 0;
                                })
                        )
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
                        .then(weaponArg("royalsword", CrownItems.BASE_ROYAL_SWORD))
                        .then(weaponArg("cutlass", CrownItems.BASE_CUTLASS))
                        .then(weaponArg("viking_axe", CrownItems.BASE_VIKING_AXE))

                        .then(argument("voteticket").executes(c -> giveTicket(false, getPlayerSender(c))))
                        .then(argument("eliteticket").executes(c -> giveTicket(true, getPlayerSender(c))))
                );
    }

    private LiteralArgument weaponArg(String arg, ItemStack item){
        return argument(arg)
                .executes(weapon(item))
                .then(levelArg(item));
    }

    private LiteralArgument saveReloadArg(SaveReloadPart saveReloadPart, boolean save){
        return argument(saveReloadPart.msg.toLowerCase().replaceAll(" ", "_")).executes(c -> saveOrReload(getSender(c), saveReloadPart, save));
    }

    private RequiredArgument<Integer> levelArg(ItemStack item){
        return argument("level", IntegerArgumentType.integer(1, 10)).executes(c -> giveWeapon(c, item, c.getArgument("level", Integer.class)));
    }

    private Command<CommandListenerWrapper> weapon(ItemStack item){
        return c -> giveWeapon(c, item, 1);
    }

    private int giveWeapon(CommandContext<CommandListenerWrapper> c, ItemStack item, int level) throws CommandSyntaxException {
        Player player = getPlayerSender(c);
        ItemStack toGive = item.clone();
        CrownWeapons.CrownWeapon weapon = CrownWeapons.fromItem(toGive);

        for (int i = 0; i < level-1; i++){
            CrownWeapons.upgradeLevel(weapon, player);
        }

        ItemMeta meta = toGive.getItemMeta();
        player.getInventory().addItem(toGive);
        broadcastAdmin(c.getSource(), "Giving " + (CrownUtils.isNullOrBlank(meta.getDisplayName()) ? CrownUtils.normalEnum(toGive.getType()) : meta.getDisplayName()));
        return level;
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
            FtcCore.getAnnouncer().start();
            broadcastAdmin(c.getSource(), "Announcer started");
            return 0;
        }

        FtcCore.getAnnouncer().stop();
        broadcastAdmin(c.getSource(), "Announcer stopped");
        return 0;
    }

    private CrownUser getUser(CommandContext<CommandListenerWrapper> c) throws CommandSyntaxException {
        return UserType.getUser(c, USER_ARG);
    }

    private int saveOrReload(CommandSender sender, SaveReloadPart thingTo, boolean save){
        thingTo.runnable.accept(save);
        if(save) broadcastAdmin(sender, thingTo.saveMessage());
        else broadcastAdmin(sender, thingTo.reloadMessage());
        return 0;
    }

    private int saveOrReloadAll(CommandSender sender, boolean save){
        for (SaveReloadPart t: SaveReloadPart.values()){
            t.runnable.accept(save);
            if(save) sender.sendMessage(t.saveMessage());
            else sender.sendMessage(t.reloadMessage());
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
            if(b) UserManager.inst().saveUsers();
            else UserManager.inst().reloadUsers();
        }),
        SHOPS ("Signshops", b ->{
            if(b) ShopManager.save();
            else ShopManager.reload();
        }),
        BLACK_MARKET ("Black Market", b -> {
            if(b) FtcCore.getBlackMarket().save();
            else FtcCore.getBlackMarket().reload();
        }),
        CONFIG ("Main Config", b -> {
            if(b) FtcCore.getInstance().saveConfig();
            else FtcCore.getInstance().reloadConfig();
        }),
        USER_MANAGER("User Manager", b ->{
            if(b) UserManager.inst().save();
            else UserManager.inst().reload();
        });

        private final String msg;
        private final Consumer<Boolean> runnable;
        SaveReloadPart(String msg, Consumer<Boolean> runnable){
            this.msg = msg;
            this.runnable = runnable;
        }

        public String reloadMessage() {
            return CrownUtils.translateHexCodes("&7" + msg + " reloaded.");
        }

        public String saveMessage() {
            return CrownUtils.translateHexCodes("&7" + msg + " saved.");
        }
    }
}