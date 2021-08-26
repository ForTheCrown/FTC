package net.forthecrown.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import io.papermc.paper.adventure.PaperAdventure;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.forthecrown.commands.arguments.PetArgument;
import net.forthecrown.commands.arguments.UserParseResult;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.marriage.CommandMarry;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.BalanceMap;
import net.forthecrown.economy.Balances;
import net.forthecrown.economy.SortedBalanceMap;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.inventory.CrownWeapons;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.user.*;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.user.enums.Faction;
import net.forthecrown.user.enums.Pet;
import net.forthecrown.user.enums.Rank;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class CommandFtcCore extends FtcCommand {

    public CommandFtcCore(){
        super("ftccore", Crown.inst());

        setDescription("The primary FTC-Core command");
        setPermission(Permissions.FTC_ADMIN);

        this.bals = Crown.getBalances();
        this.maxMoney = ComVars.getMaxMoneyAmount();

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

                /*.then(literal("update_spectator")
                        .executes(c -> {
                            UserManager.updateSpectatorTab();

                            c.getSource().sendAdmin("Updating specators in tab");
                            return 0;
                        })
                )*/

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

                //Everything relating to a specific user
                .then(literal("user")
                        .then(argument(USER_ARG, UserArgument.user())

                                //Save the user's data
                                .then(literal("save")
                                        .executes(c -> {
                                            CrownUser u = getUser(c);
                                            u.save();
                                            c.getSource().sendAdmin( "Saved data of " + u.getName());
                                            return 0;
                                        })
                                )

                                //Reload the user's data
                                .then(literal("reload")
                                        .executes(c -> {
                                            CrownUser u = getUser(c);
                                            u.reload();
                                            c.getSource().sendAdmin( "Reloaded data of " + u.getName());
                                            return 0;
                                        })
                                )

                                .then(CommandLore.compOrStringArg(literal("prefix"), (c, b) -> Suggestions.empty(), (c, prefix) -> {
                                    CrownUser user = getUser(c);

                                    user.setCurrentPrefix(prefix);

                                    c.getSource().sendMessage(
                                            Component.text("Set ")
                                                    .append(user.displayName())
                                                    .append(Component.text("'s prefix to be "))
                                                    .append(prefix)
                                    );
                                    return 0;
                                }))

                                //This alt shit can go fuck itself
                                .then(literal("alt")
                                        .then(literal("list")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);
                                                    UserManager um = Crown.getUserManager();

                                                    c.getSource().sendMessage(
                                                            Component.text(user.getName() + "'s alt accounts:")
                                                            .append(Component.newline())
                                                            .append(Component.text(um.getAlts(user.getUniqueId()).toString()))
                                                    );

                                                    return 0;
                                                })
                                        )
                                        .then(literal("for")
                                                .then(argument("altFor", UserArgument.user())

                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);
                                                            CrownUser main = c.getArgument("altFor", UserParseResult.class).getUser(c.getSource(), false);
                                                            UserManager um = Crown.getUserManager();

                                                            um.addEntry(user.getUniqueId(), main.getUniqueId());
                                                            user.unload();
                                                            CrownUserAlt alt = new FtcUserAlt(user.getUniqueId(), main.getUniqueId());
                                                            alt.save();

                                                            c.getSource().sendAdmin( alt.getName() + " is now an alt for " + main.getName());
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(literal("for_none")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);
                                                    UserManager um = Crown.getUserManager();

                                                    if(!um.isAlt(user.getUniqueId())) throw FtcExceptionProvider.create(user.getName() + " is not an alt");
                                                    um.removeEntry(user.getUniqueId());

                                                    c.getSource().sendAdmin( user.getName() + " is no longer an alt");
                                                    return 0;
                                                })
                                        )
                                )

                                .then(literal("married")
                                        .executes(c -> {
                                            CrownUser user = getUser(c);
                                            UserInteractions inter = user.getInteractions();

                                            if(inter.getSpouse() == null) throw FtcExceptionProvider.create(user.getName() + " is not married");

                                            c.getSource().sendMessage(
                                                    Component.text()
                                                            .append(user.displayName())
                                                            .append(Component.text(" is married to "))
                                                            .append(UserManager.getUser(inter.getSpouse()).displayName())
                                                            .append(Component.text("."))
                                                            .build()
                                            );
                                            return 0;
                                        })

                                        .then(literal("resetCooldown")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);
                                                    user.getInteractions().setLastMarriageChange(0L);

                                                    c.getSource().sendAdmin(
                                                            Component.text("Reset cooldown of ")
                                                                    .append(user.displayName())
                                                    );
                                                    return 0;
                                                })
                                        )

                                        .then(literal("divorce")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);

                                                    if(user.getInteractions().getSpouse() == null) throw FtcExceptionProvider.create("User is not married");

                                                    CrownUser spouse = UserManager.getUser(user.getInteractions().getSpouse());

                                                    spouse.getInteractions().setSpouse(null);
                                                    user.getInteractions().setSpouse(null);

                                                    c.getSource().sendAdmin(
                                                            Component.text("Made ")
                                                                    .append(user.displayName())
                                                                    .append(Component.text(" divorce"))
                                                    );
                                                    return 0;
                                                })
                                        )

                                        .then(argument("target", UserArgument.user())
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);
                                                    CrownUser target = UserArgument.getUser(c, "target");

                                                    if(user.getUniqueId().equals(target.getUniqueId())) throw FtcExceptionProvider.create("Cannot make people marry themselves lol");

                                                    user.getInteractions().setSpouse(target.getUniqueId());
                                                    target.getInteractions().setSpouse(user.getUniqueId());

                                                    c.getSource().sendAdmin(
                                                            Component.text("Married ")
                                                                    .append(user.displayName())
                                                                    .append(Component.text(" to "))
                                                                    .append(target.displayName())
                                                    );
                                                    return 0;
                                                })
                                        )
                                )

                                .then(literal("balance")
                                        .executes(c-> { //Shows the balance
                                            CrownUser user = getUser(c);
                                            c.getSource().sendMessage(user.getName() + " has " + FtcFormatter.getRhines(bals.get(user.getUniqueId())) + " Rhines");
                                            return 0;
                                        })

                                        //Sets the balance
                                        .then(literal("set")
                                                .then(argument("sAmount", IntegerArgumentType.integer(0, maxMoney))
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);

                                                            int amount = c.getArgument("sAmount", Integer.class);
                                                            bals.set(user.getUniqueId(), amount);

                                                            c.getSource().sendAdmin( "Set " + user.getName() + "'s balance to " + FtcFormatter.getRhines(bals.get(user.getUniqueId())));
                                                            return 0;
                                                        })
                                                )
                                        )
                                        //Adds to the balance
                                        .then(literal("add")
                                                .then(argument("aAmount", IntegerArgumentType.integer(1, maxMoney))
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);

                                                            int amount = c.getArgument("aAmount", Integer.class);
                                                            bals.add(user.getUniqueId(), amount, false);

                                                            c.getSource().sendAdmin( "Added " + amount + " to " + user.getName() + "'s balance.");
                                                            c.getSource().sendAdmin( "Now has " + FtcFormatter.getRhines(bals.get(user.getUniqueId())));
                                                            return 0;
                                                        })
                                                )
                                        )
                                        //Removes from the balance
                                        .then(literal("remove")
                                                .then(argument("rAmount", IntegerArgumentType.integer(1, maxMoney))
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);

                                                            int amount = c.getArgument("rAmount", Integer.class);
                                                            bals.add(user.getUniqueId(), -amount, false);

                                                            c.getSource().sendAdmin( "Removed " + amount + " from " + user.getName() + "'s balance.");
                                                            c.getSource().sendAdmin( "Now has " + FtcFormatter.getRhines(bals.get(user.getUniqueId())));
                                                            return 0;
                                                        })
                                                )
                                        )
                                        //Resets the balance, removes it from the BalanceMap so it doesn't take up as much data
                                        .then(literal("reset")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);

                                                    Balances bals = Crown.getBalances();
                                                    BalanceMap balMap = bals.getMap();

                                                    balMap.remove(user.getUniqueId());
                                                    bals.setMap((SortedBalanceMap) balMap);

                                                    c.getSource().sendAdmin( "Reset balance of " + user.getName());
                                                    return 0;
                                                })
                                        )
                                )

                                .then(literal("afk")
                                        .executes(c -> {
                                            CrownUser user = getUser(c);

                                            c.getSource().sendMessage(
                                                    Component.text()
                                                            .append(user.displayName())
                                                            .append(Component.text(" is AFK: "))
                                                            .append(Component.text(user.isAfk()))
                                                            .build()
                                            );
                                            return 0;
                                        })

                                        .then(argument("bool", BoolArgumentType.bool())
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);
                                                    boolean bool = c.getArgument("bool", Boolean.class);

                                                    user.setAfk(bool, null);

                                                    c.getSource().sendMessage(
                                                            Component.text("Set ")
                                                                    .append(user.displayName())
                                                                    .append(Component.text(" afk: "))
                                                                    .append(Component.text(bool))
                                                    );
                                                    return 0;
                                                })
                                        )
                                )

                                .then(literal("baron")
                                        .then(argument("isBaron", BoolArgumentType.bool())
                                                .suggests(suggestMatching("true", "false"))

                                                .executes(c ->{
                                                    CrownUser user = getUser(c);
                                                    boolean isBaron = c.getArgument("isBaron", Boolean.class);
                                                    if(user.isBaron() == isBaron) throw FtcExceptionProvider.create(user.getName() + "'s baron value is the same as entered!");

                                                    user.setBaron(isBaron);
                                                    c.getSource().sendMessage(user.getName() + " isBaron " + user.isBaron());
                                                    return 0;
                                                })
                                        )
                                        .executes(c ->{
                                            CrownUser user = getUser(c);
                                            c.getSource().sendMessage(user.getName() + " is baron: " + user.isBaron());
                                            return 0;
                                        })
                                )
                                .then(literal("pets")
                                        .then(literal("list")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);

                                                    net.minecraft.network.chat.Component component = new TextComponent(user.getName() + "'s pets: " +
                                                            ListUtils.join(user.getPets(), pet -> pet.toString().toLowerCase())
                                                    );

                                                    c.getSource().sendMessage(PaperAdventure.asAdventure(component));
                                                    return 0;
                                                })
                                        )
                                        .then(argument("pet", PetArgument.PET)

                                                .then(literal("add")
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);
                                                            Pet pet = c.getArgument("pet", Pet.class);

                                                            if(user.hasPet(pet)) throw FtcExceptionProvider.create(user.getName() + " already has that pet");

                                                            user.addPet(pet);
                                                            c.getSource().sendAdmin( "Added " + pet.toString() + " to " + user.getName());
                                                            return 0;
                                                        })
                                                )
                                                .then(literal("remove")
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);
                                                            Pet pet = c.getArgument("pet", Pet.class);

                                                            if(!user.hasPet(pet)) throw FtcExceptionProvider.create(user.getName() + " doesns't have that pet");

                                                            user.removePet(pet);
                                                            c.getSource().sendAdmin( "Removed " + pet.toString() + " from " + user.getName());
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                                .then(literal("rank")
                                        .executes(c -> {
                                            CrownUser user = getUser(c);
                                            c.getSource().sendMessage(user.getName() + "'s rank is " + user.getRank().getPrefix());
                                            return 0;
                                        })

                                        .then(literal("set")
                                                .then(argument("rankToSet", FtcCommands.RANK)
                                                        .executes(c -> {
                                                            CrownUser user = getUser(c);
                                                            Rank rank = c.getArgument("rankToSet", Rank.class);

                                                            user.setRank(rank, true);

                                                            c.getSource().sendAdmin("Set rank of " + user.getName() + " to " + rank.getPrefix());
                                                            return 0;
                                                        })
                                                )
                                        )

                                        .then(literal("list")
                                                .executes(c -> {
                                                    CrownUser user = getUser(c);
                                                    c.getSource().sendMessage(user.getName() + "'s ranks: " + ListUtils.join(user.getAvailableRanks(), r -> r.name().toLowerCase()));
                                                    return 0;
                                                })
                                        )
                                        .then(literal("add")
                                                .then(argument("rankToAdd", FtcCommands.RANK)
                                                        .executes(c ->{
                                                            CrownUser user = getUser(c);
                                                            Rank rank = c.getArgument("rankToAdd", Rank.class);

                                                            user.addRank(rank);
                                                            c.getSource().sendAdmin( user.getName() + " now has " + rank.getPrefix());
                                                            return 0;
                                                        })
                                                )
                                        )
                                        .then(literal("remove")
                                                .then(argument("rankToRemove", FtcCommands.RANK)
                                                        .executes(c ->{
                                                            CrownUser user = getUser(c);
                                                            Rank rank = c.getArgument("rankToRemove", Rank.class);

                                                            user.removeRank(rank);
                                                            c.getSource().sendAdmin( user.getName() + " no longer has " + rank.getPrefix());
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                                .then(literal("branch")
                                        .executes(c ->{
                                            CrownUser user = getUser(c);
                                            c.getSource().sendMessage(user.getName() + "'s branch is " + user.getFaction().toString());
                                            return 0;
                                        })
                                        .then(argument("branchToSet", FtcCommands.BRANCH)
                                                .executes(c ->{
                                                    CrownUser user = getUser(c);
                                                    Faction faction = c.getArgument("branchToSet", Faction.class);
                                                    user.setFaction(faction);
                                                    c.getSource().sendAdmin( user.getName() + " is now a " + user.getFaction().getSingularName());
                                                    return 0;
                                                })
                                        )
                                )
                                .then(literal("addgems")
                                        .then(argument("gemAmount", IntegerArgumentType.integer())
                                                .executes(c ->{
                                                    CrownUser user = getUser(c);
                                                    int gems = c.getArgument("gemAmount", Integer.class);

                                                    user.addGems(gems);
                                                    c.getSource().sendAdmin( user.getName() + " now has " + user.getGems() + " gems");

                                                    return 0;
                                                })
                                        )
                                )
                                .then(literal("resetearnings")
                                        .executes(c ->{
                                            CrownUser u = getUser(c);
                                            u.resetEarnings();
                                            c.getSource().sendAdmin( u.getName() + " earnings reset");
                                            return 0;
                                        })
                                )
                                .then(literal("delete")
                                        .executes(c ->{
                                            CrownUser user = getUser(c);

                                            user.delete();
                                            user.unload();
                                            c.getSource().sendAdmin(user.getName() + "'s user data has been deleted");
                                            return 0;
                                        })
                                )
                        )
                )
                .then(literal("announcer")
                        .then(literal("start").executes(c -> announcerThing(c, true)))
                        .then(literal("stop").executes(c -> announcerThing(c, false)))
                        .then(literal("announce_all")
                                .executes(c -> {
                                    Announcer announcer = Crown.getAnnouncer();

                                    for (Component comp: announcer.getAnnouncements()){
                                        announcer.announce(comp);
                                    }

                                    c.getSource().sendAdmin( "All announcements have been broadcast");
                                    return 0;
                                })
                        )
                )

                .then(literal("item")
                        .then(literal("crown")
                                .then(argument("level", IntegerArgumentType.integer(1, 6))
                                        .then(argument("owner", StringArgumentType.greedyString())
                                                .executes(c ->{
                                                    Player player = getPlayerSender(c);
                                                    int level = c.getArgument("level", Integer.class);
                                                    String owner = c.getArgument("owner", String.class);

                                                    player.getInventory().addItem(CrownItems.makeCrown(level, owner));
                                                    c.getSource().sendMessage("You got a level " + level + " crown");

                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(literal("coin")
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(c -> giveCoins(getPlayerSender(c), c.getArgument("amount", Integer.class)))
                                )
                                .executes(c -> giveCoins(getPlayerSender(c), 100))
                        )
                        .then(weaponArg("royalsword", CrownItems.royalSword()))
                        .then(weaponArg("cutlass", CrownItems.cutlass()))
                        .then(weaponArg("viking_axe", CrownItems.vikingAxe()))

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

    private LiteralArgumentBuilder<CommandSource> weaponArg(String arg, ItemStack item){
        return literal(arg)
                .executes(weapon(item))
                .then(levelArg(item));
    }

    private RequiredArgumentBuilder<CommandSource, Integer> levelArg(ItemStack item){
        return argument("level", IntegerArgumentType.integer(1, 10)).executes(c -> giveWeapon(c, item, c.getArgument("level", Integer.class)));
    }

    private Command<CommandSource> weapon(ItemStack item){
        return c -> giveWeapon(c, item, 1);
    }

    private int giveWeapon(CommandContext<CommandSource> c, ItemStack item, int level) throws CommandSyntaxException {
        Player player = getPlayerSender(c);
        ItemStack toGive = item.clone();
        CrownWeapons.Weapon weapon = CrownWeapons.fromItem(toGive);

        for (int i = 0; i < level-1; i++){
            CrownWeapons.upgradeLevel(weapon, player);
        }

        ItemMeta meta = toGive.getItemMeta();
        player.getInventory().addItem(toGive);
        c.getSource().sendAdmin( "Giving " + (FtcUtils.isNullOrBlank(meta.getDisplayName()) ? FtcFormatter.normalEnum(toGive.getType()) : meta.getDisplayName()));
        return level;
    }

    private int giveTicket(boolean elite, Player player, CommandSource source){
        try {
            if(elite) player.getInventory().addItem(CrownItems.eliteVoteTicket());
            else player.getInventory().addItem(CrownItems.voteTicket());
        } catch (Exception e){
            player.sendMessage("Inventory full");
            return 0;
        }
        source.sendAdmin("Giving vote ticket");
        return 0;
    }

    private int giveCoins(Player player, int amount){
        player.getInventory().addItem(CrownItems.makeCoins(amount, 1));
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
        REGIONS ("Regions", b -> {
            if(b) Crown.getRegionManager().reload();
            else Crown.getRegionManager().save();
        }),
        ITEM_PRICES ("Item Prices", b -> {
            if(b) Crown.getPriceMap().reload();
            else Crown.getPriceMap().save();
        }),
        GRAPPLING_HOOK ("Grappling Hook", b -> {
            if(b) Pirates.getParkour().reload();
            else Pirates.getParkour().save();
        }),
        PARROT_TRACKER ("Parrot Tracker", b -> {
            if(b) Pirates.getParrotTracker().reload();
            else Pirates.getParrotTracker().save();
        }),
        TREASURE_SHULKER ("Treasure Shulker", b -> {
            if(b) Pirates.getTreasure().reload();
            else Pirates.getTreasure().save();
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
            if(b) Crown.getBalances().save();
            else Crown.getBalances().reload();
        }),
        USERS ("Users", b -> {
            if(b) UserManager.inst().saveUsers();
            else UserManager.inst().reloadUsers();
        }),
        SHOPS ("Signshops", b ->{
            if(b) Crown.getShopManager().save();
            else Crown.getShopManager().reload();
        }),
        BLACK_MARKET ("Black Market", b -> {
            if(b) Pirates.getPirateEconomy().save();
            else Pirates.getPirateEconomy().reload();
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
            return FtcFormatter.translateHexCodes("&7" + msg + " reloaded.");
        }

        public String saveMessage() {
            return FtcFormatter.translateHexCodes("&7" + msg + " saved.");
        }
    }
}