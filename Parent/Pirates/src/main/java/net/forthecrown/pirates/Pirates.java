package net.forthecrown.pirates;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CommandLeave;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.pirates.auctions.Auction;
import net.forthecrown.pirates.auctions.AuctionManager;
import net.forthecrown.pirates.commands.*;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Pirates extends JavaPlugin implements Listener {

    //public File wilhelmFile;
    //public YamlConfiguration wilhelmYaml;
    //public List<String> players = new ArrayList<String>();
    //public List<ItemStack> itemsToGet = new ArrayList<ItemStack>();
    public File offlineWithParrots;
    public static Pirates plugin;
    public GrapplingHook grapplingHook;

    public PirateEvents events;

    private static AuctionManager auctionManager;

    public void onEnable() {
        plugin = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Check yaml
        offlineWithParrots = new File(getDataFolder(), "Offline_With_Parrot_Players.yml");
        if(!offlineWithParrots.exists()){
            try {
                offlineWithParrots.createNewFile();
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineWithParrots);
                yaml.createSection("Players");
                yaml.set("Players", new ArrayList<String>());
                saveyaml(yaml, offlineWithParrots);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //types
        grapplingHook = new GrapplingHook(this);
        auctionManager = new AuctionManager(this);
        events = new PirateEvents(this);

        //commands
        new CommandGhTarget();
        new CommandGhShowName();
        new CommandParrot();
        new CommandUpdateLeaderboard();
        new CommandPirateReload();

        CommandLeave.addAllowedArea(
                new CrownBoundingBox(CrownUtils.WORLD_VOID, -5685, 1, -521, -886, 255, 95),
                new Location(CrownUtils.WORLD_VOID, -800.5, 232, 11.5, -90, 0),
                plr -> {
                    plr.getInventory().clear();
                    return true;
                }
        );

        //events
        getServer().getPluginManager().registerEvents(events, this);
        getServer().getPluginManager().registerEvents(new BaseEgg(), this);
        getServer().getPluginManager().registerEvents(new NpcSmithEvent(), this);

        updateDate();
    }

    @SuppressWarnings("deprecation")
    public void onDisable() {
        List<String> players = new ArrayList<>();
        for (UUID playeruuid : events.parrots.values()) {
            try { // Online while reload
                Bukkit.getPlayer(playeruuid).setShoulderEntityLeft(null);
            }
            catch (Exception e) { // Offline while reload
                players.add(playeruuid.toString());
            }
        }
        events.parrots.clear();
        auctionManager.saveAuctions();

        for (Auction a: AuctionManager.getAuctions().values()){
            a.removeDisplay();
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineWithParrots);
        yaml.set("Players", players);
        saveyaml(yaml, offlineWithParrots);
    }

    public static AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public void updateDate() {
        Calendar cal = Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (cal.get(Calendar.DAY_OF_WEEK) != getConfig().getInt("Day")) {

                getConfig().set("Day", cal.get(Calendar.DAY_OF_WEEK));
                ItemStack chosenItem = getRandomHeadFromChest();
                Validate.notNull(chosenItem, "ChosenItem was null");
                Validate.notNull(((SkullMeta)chosenItem.getItemMeta()).getPlayerProfile(), "player profile was null");
                Validate.notNull(((SkullMeta)chosenItem.getItemMeta()).getPlayerProfile().getName(), "player profile name was null");
                getConfig().set("ChosenHead", ((SkullMeta) chosenItem.getItemMeta()).getPlayerProfile().getName());

                List<String> temp = getConfig().getStringList("PlayerWhoSoldHeadAlready");
                temp.clear();
                getConfig().set("PlayerWhoSoldHeadAlready", temp);
                getConfig().set("PlayerWhoFoundTreasureAlready", temp);

                killOldTreasure(new Location(Bukkit.getWorld(getConfig().getString("TreasureLoc.world")), getConfig().getInt("TreasureLoc.x"), getConfig().getInt("TreasureLoc.y"), getConfig().getInt("TreasureLoc.z")));

                final int x = CrownUtils.getRandomNumberInRange(-1970, 1970);
                final int y = CrownUtils.getRandomNumberInRange(40, 50);
                final int z = CrownUtils.getRandomNumberInRange(-1970, 1970);
                //int x = getRandomNumberInRange(-50, 50);
                //int y = getRandomNumberInRange(40, 50);
                //int z = getRandomNumberInRange(-50, 50);
                getConfig().set("TreasureLoc.x", x);
                getConfig().set("TreasureLoc.y", y);
                getConfig().set("TreasureLoc.z", z);

                spawnTreasureShulker(new Location(Bukkit.getWorld(getConfig().getString("TreasureLoc.world")), x, y, z));

                saveConfig();
            }

            if (cal.get(Calendar.WEEK_OF_MONTH) != getConfig().getInt("Week")) {
                // Picks a day for the trader to spawn.
                getConfig().set("Week", cal.get(Calendar.WEEK_OF_MONTH));
                saveConfig();
            }
        }, 20L);
    }


    protected void killOldTreasure(Location location) {
        for (Entity nearbyEntity : location.getWorld().getNearbyEntities(location, 1, 1, 1))
        {
            if (nearbyEntity.getType() == EntityType.SHULKER)
            {
                nearbyEntity.remove();
            }
        }
    }

    protected void spawnTreasureShulker(Location spawnLoc) {
        spawnLoc.getBlock().setType(Material.AIR);
        Shulker treasureShulker = spawnLoc.getWorld().spawn(spawnLoc, Shulker.class);
        treasureShulker.setAI(false);
        treasureShulker.setInvulnerable(true);
        treasureShulker.setColor(DyeColor.GRAY);
        treasureShulker.setRemoveWhenFarAway(false);
        treasureShulker.setPersistent(true);

    }

    void giveTreasure(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

        double moneyDecider = Math.random();
        if (moneyDecider <= 0.6) {
            FtcCore.getBalances().add(player.getUniqueId(), 5000, false);
            player.sendMessage(ChatColor.GRAY + "You've found a treasure with " + ChatColor.YELLOW + "5,000 rhines" + ChatColor.GRAY + " inside.");
            Bukkit.dispatchCommand(getServer().getConsoleSender(), "crate givekey " + player.getName() + " lootbox1 1");
        }
        else if (moneyDecider > 0.6 && moneyDecider <= 0.9) {
            FtcCore.getBalances().add(player.getUniqueId(), 10000, false);
            player.sendMessage(ChatColor.GRAY + "You've found a treasure with " + ChatColor.YELLOW + "10,000 rhines" + ChatColor.GRAY + " inside.");
            Bukkit.dispatchCommand(getServer().getConsoleSender(), "crate givekey " + player.getName() + " lootbox1 2");
        }
        else {
            FtcCore.getBalances().add(player.getUniqueId(), 20000, false);
            player.sendMessage(ChatColor.GRAY + "You've found a treasure with " + ChatColor.YELLOW + "20,000 rhines" + ChatColor.GRAY + " inside.");
            Bukkit.dispatchCommand(getServer().getConsoleSender(), "crate givekey " + player.getName() + " lootbox1 3");
        }

        List<ItemStack> commonItems = getItems(((Chest) Bukkit.getWorld("world").getBlockAt(
                new Location(Bukkit.getWorld(getConfig().getString("TreasureCommonLoot.world")), getConfig().getInt("TreasureCommonLoot.x"), getConfig().getInt("TreasureCommonLoot.y"), getConfig().getInt("TreasureCommonLoot.z"))).getState()));
        List<ItemStack> rareItems = getItems(((Chest) Bukkit.getWorld("world").getBlockAt(
                new Location(Bukkit.getWorld(getConfig().getString("TreasureRareLoot.world")), getConfig().getInt("TreasureRareLoot.x"), getConfig().getInt("TreasureRareLoot.y"), getConfig().getInt("TreasureRareLoot.z"))).getState()));
        List<ItemStack> specialItems = getItems(((Chest) Bukkit.getWorld("world").getBlockAt(
                new Location(Bukkit.getWorld(getConfig().getString("TreasureSpecialLoot.world")), getConfig().getInt("TreasureSpecialLoot.x"), getConfig().getInt("TreasureSpecialLoot.y"), getConfig().getInt("TreasureSpecialLoot.z"))).getState()));

        for (int i = 0; i < 6; i++) {
            double random = Math.random();
            ItemStack chosenItem;

            if (random <= 0.6) {
                chosenItem = getItemFromList(commonItems);
                //Bukkit.broadcastMessage("Common: " + chosenItem.getType().toString().toLowerCase());
            }
            else if (random > 0.6 && random <= 0.9) {
                chosenItem = getItemFromList(rareItems);
                //Bukkit.broadcastMessage("Rare: " + chosenItem.getType().toString().toLowerCase());
            }
            else {
                chosenItem = getItemFromList(specialItems);
                //Bukkit.broadcastMessage("Special: " + chosenItem.getType().toString().toLowerCase());
            }

            player.getInventory().addItem(chosenItem);
        }

        givePP(player, 1);

    }


    @SuppressWarnings("deprecation")
    public void givePP(Player player, int toadd) {
        Objective pp = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("PiratePoints");
        Score ppp = pp.getScore(player);
        ppp.setScore(ppp.getScore() + toadd);

        CrownUser user = FtcCore.getUser(player.getUniqueId());

        if (ppp.getScore() == 1) player.sendMessage(ChatColor.GRAY + "[FTC] You now have " + ChatColor.YELLOW + ppp.getScore() + ChatColor.GRAY + " Pirate point.");
        else {
            player.sendMessage(ChatColor.GRAY+ "You now have " + ChatColor.YELLOW + ppp.getScore() + ChatColor.GRAY + " Pirate points.");

            // Check for sailor / pirate
            if (ppp.getScore() >= 10 && !user.hasRank(Rank.SAILOR)) {

                user.addRank(Rank.SAILOR);
                Bukkit.dispatchCommand(getServer().getConsoleSender(), "lp user " + player.getName() + " parent add free-rank");

                player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);

                for (int i = 0; i <= 5; i++) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().getX(), player.getLocation().getY()+2, player.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d), i*5L);
                }
                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "You've been promoted to " + ChatColor.DARK_GRAY + ChatColor.BOLD + "{" + ChatColor.GRAY + "Sailor" + ChatColor.DARK_GRAY + ChatColor.BOLD + "}" + ChatColor.WHITE + " !");
                player.sendMessage(ChatColor.WHITE + "You can now select the tag in " + ChatColor.YELLOW + "/rank" + ChatColor.WHITE + " now.");
            }
            else if (ppp.getScore() >= 50 && !user.hasRank(Rank.PIRATE)) {

                user.addRank(Rank.PIRATE);

                player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);

                for (int i = 0; i <= 5; i++) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().getX(), player.getLocation().getY()+2, player.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d), i*5L);
                }
                player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "You've been promoted to " + ChatColor.DARK_GRAY + ChatColor.BOLD + "{" + ChatColor.GRAY + "Pirate" + ChatColor.DARK_GRAY + ChatColor.BOLD + "}" + ChatColor.WHITE + " !");
                player.sendMessage(ChatColor.WHITE + "You can now select the tag in " + ChatColor.YELLOW + "/rank" + ChatColor.WHITE + " now.");
            }
        }

        //updateLeaderBoard();
    }


    private void spawnLeaderboard(int amount) {
        removeLeaderboard();
        List<String> top = getTopPlayers(Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("PiratePoints"), amount);
        double distanceBetween = 0.27;

        for (int i = 0; i < top.size(); i++)
        {
            spawnArmorStand(getLeaderboardLoc(), distanceBetween*i, top.get(top.size()-i-1), true);
        }

        spawnArmorStand(getLeaderboardLoc(), distanceBetween*top.size(), ChatColor.GOLD + "---------=o=O=o=---------", false);
        spawnArmorStand(getLeaderboardLoc(), distanceBetween*(top.size()+1), ChatColor.WHITE + "Pirate Points Leaderboard", false);
        spawnArmorStand(getLeaderboardLoc(), -distanceBetween, ChatColor.GOLD + "---------=o=O=o=---------", false);
    }

    private void spawnArmorStand(Location loc, double d, String text, boolean isScoreStand) {
        ArmorStand armorstand = loc.getWorld().spawn(loc.add(0, d, 0), ArmorStand.class);
        armorstand.setGravity(false);
        armorstand.setVisible(false);
        armorstand.setCustomName(text);
        armorstand.setCustomNameVisible(true);
        getAllLeaderboardArmorstands().add(armorstand);
        if (isScoreStand) getLeaderBoardArmorStands().add(armorstand);
    }

    private void removeLeaderboard() {
        for (ArmorStand armorstand : getAllLeaderboardArmorstands())
        {
            armorstand.remove();
        }
        allLeaderboardArmorstands.clear();
        for (Entity ent : getLeaderboardLoc().getWorld().getNearbyEntities(getLeaderboardLoc(), 0.1, 5, 0.1))
        {
            if (ent instanceof ArmorStand)
                ent.remove();
        }
    }


    private final List<ArmorStand> leaderboardArmorstands = new ArrayList<>();
    public List<ArmorStand> getLeaderBoardArmorStands() {
        return leaderboardArmorstands;
    }
    private final List<ArmorStand> allLeaderboardArmorstands = new ArrayList<>();
    public List<ArmorStand> getAllLeaderboardArmorstands() {
        return allLeaderboardArmorstands;
    }

    public Location getLeaderboardLoc() {
        return new Location(Bukkit.getWorld("world"), -639.0, 70, 3830.5, 90, 0); // TODO UPDATE?
    }

    public void updateLeaderBoard() {
        removeLeaderboard();
        spawnLeaderboard(5);
		/*List<String> top = getTopPlayers(Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("PiratePoints"), getLeaderBoardArmorStands().size());
		for (int i = 0; i < top.size(); i++)
		{
			getLeaderBoardArmorStands().get(i).setCustomName( top.get(top.size()-i-1));
		}*/

    }

    private List<ItemStack> getItems(Chest chest) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack item : chest.getInventory().getContents())
        {
            if (item != null) result.add(item);
        }
        return result;
    }

    private ItemStack getItemFromList(List<ItemStack> list) {
        ItemStack result;

        int index = CrownUtils.getRandomNumberInRange(0, list.size()-1);
        result = list.get(index);
        int count = 0;
        while (list.contains(result) && (count++ != list.size())) {
            result = list.get(++index % list.size());
        }

        return result;
    }

    public ItemStack getRandomHeadFromChest() {
        Location[] chestLocs = {
                getloc("HeadChestLocation1"),
                getloc("HeadChestLocation2"),
                getloc("HeadChestLocation3"),
                getloc("HeadChestLocation4")
        };

        for (Location l: chestLocs){
            if(l.getBlock().getType() != Material.CHEST) throw new NullPointerException(l.toString() + " is not a chest");
        }

        int slot = CrownUtils.getRandomNumberInRange(0, 26);
        Location chosenLoc = chestLocs[CrownUtils.getRandomNumberInRange(0, 3)];

        ItemStack chosenItem = ((Chest) Bukkit.getWorld("world").getBlockAt(chosenLoc).getState()).getInventory().getContents()[slot];
        return Objects.requireNonNullElseGet(chosenItem, () -> new ItemStack(Material.STONE));
    }

    private Location getloc(String section) {
        return new Location(Bukkit.getWorld(getConfig().getString(section + ".world")), getConfig().getInt(section + ".x"), getConfig().getInt(section + ".y"), getConfig().getInt(section + ".z"));
    }

    void giveReward(Player player) {
        FtcCore.getBalances().add(player.getUniqueId(), 10000, false);
        player.sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + "10,000 rhines" + ChatColor.GRAY + " from " + ChatColor.YELLOW + "Wilhelm" + ChatColor.GRAY + ".");
        givePP(player, 2);
    }


    @SuppressWarnings("deprecation")
    boolean checkIfInvContainsHead(PlayerInventory inv) {
        int size = 36;

        for (int i = 0; i < size; i++) {
            ItemStack invItem = inv.getItem(i);
            if (invItem != null) {
                if (invItem.getType() == Material.PLAYER_HEAD) {
                    if (invItem.hasItemMeta() && ((SkullMeta) invItem.getItemMeta()).getOwner().equalsIgnoreCase(getConfig().getString("ChosenHead"))) {
                        invItem.setAmount(invItem.getAmount()-1);
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public List<String> getTopPlayers(Objective objective, int top) {
        List<String> unsortedResult = new ArrayList<>();
        int score;
        for(String name : objective.getScoreboard().getEntries()) {
            if (unsortedResult.size() < top) {
                unsortedResult.add(name);
            }
            else {
                score = objective.getScore(name).getScore();
                for (String nameInList : unsortedResult) {
                    if (score > objective.getScore(nameInList).getScore()) {
                        String lowestPlayer = nameInList;
                        for (String temp : unsortedResult) {
                            if (objective.getScore(temp).getScore() < objective.getScore(lowestPlayer).getScore()) {
                                lowestPlayer = temp;
                            }
                        }
                        unsortedResult.remove(lowestPlayer);
                        unsortedResult.add(name);
                        break;
                    }
                }
            }
        }

        List<String> sortedResult = new ArrayList<>();

        String playername = null;
        int size = unsortedResult.size();
        for (int j = 1; j <= size; j++) {
            int max = Integer.MIN_VALUE;


            // Zoek max in result
            for (String s : unsortedResult) {
                if (objective.getScore(s).getScore() > max) {
                    max = objective.getScore(s).getScore();
                    playername = s;
                }
            }

            unsortedResult.remove(playername);
            /*if (objective.getScore(playername).getScore() != 0) */sortedResult.add(j + ". " + ChatColor.YELLOW + playername + ChatColor.WHITE + " - " + objective.getScore(playername).getScore());
        }

        return sortedResult;
    }

	/*@SuppressWarnings("deprecation")
	@EventHandler
	public void parrotCarrierLogsOut(PlayerQuitEvent event) {
		if (parrots.containsValue(event.getPlayer().getUniqueId()))
		{
			parrots.remove(event.getPlayer().getShoulderEntityLeft().getUniqueId());
			event.getPlayer().setShoulderEntityLeft(null);

		}
	}*/

    public void saveyaml(FileConfiguration yaml, File file) {
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
