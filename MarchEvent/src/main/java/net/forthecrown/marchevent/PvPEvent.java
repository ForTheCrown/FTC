package net.forthecrown.marchevent;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.marchevent.commands.CrownGameCommand;
import net.forthecrown.marchevent.events.InEventListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class PvPEvent {

    public static Set<Player> inEvent = new HashSet<>();

    public static final List<ItemStack> ITEM_LIST = Arrays.asList(
            new ItemStack(Material.STONE_SWORD, 1),
            new ItemStack(Material.ARROW, 6),
            new ItemStack(Material.BOW),
            new ItemStack(Material.SHEARS)
    );

    private final InEventListener inEventListener = new InEventListener();

    public static final BoundingBox YELLOW_ENTRY_BOX = new BoundingBox(-750, 81, 1007, -747, 70, 1003);
    public static final BoundingBox BLUE_ENTRY_BOX = new BoundingBox(-738, 81, 1007, -735, 70, 1003);

    public static final World EVENT_WORLD = Bukkit.getWorld("world");
    public static final Location YELLOW_START = new Location(EVENT_WORLD, -770, 77, 1031, -90, 0);
    public static final Location BLUE_START = new Location(EVENT_WORLD, -716, 77, 1031, 90, 0);

    public static final Location EXIT_LOCATION = new Location(EVENT_WORLD, -743, 80, 1004.5);
    public static final Location CENTER_LOCATION = new Location(EVENT_WORLD, -744, 69, 1030);
    public static final Location DOOR_LOCATION = new Location(EVENT_WORLD, -720, 76, 1030);

    public static final Set<Player> BLUE_TEAM = new HashSet<>();
    public static final Set<Player> YELLOW_TEAM = new HashSet<>();

    public static final CrownBoundingBox ARENA_VICINITY = new CrownBoundingBox(EVENT_WORLD, -782, 0, 995, -704, 255, 1070);

    private static BossBar bar;
    private int tickerId;

    public static Team yellowTeam;
    public static Team blueTeam;

    public PvPEvent(){
        yellowTeam = EventMain.getInstance().getServer().getScoreboardManager().getMainScoreboard().getTeam("yellowTeam");
        blueTeam = EventMain.getInstance().getServer().getScoreboardManager().getMainScoreboard().getTeam("blueTeam");

        yellowTeam.setAllowFriendlyFire(false);
        blueTeam.setAllowFriendlyFire(false);
    }

    public static void tellPlayersInVicinity(String message){
        List<Player> players = ARENA_VICINITY.getPlayersIn();
        message = CrownUtils.translateHexCodes(message);

        for (Player p: players){
            p.sendMessage(message);
        }
        Announcer.log(Level.INFO, message);
    }

    public void startEvent(boolean countdown){
        tellPlayersInVicinity("&eRound starting!");
        EventMain.getInstance().getServer().getPluginManager().registerEvents(inEventListener, EventMain.getInstance());
        logGame();

        resetEvent(false);
        bar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_10);
        bar.setProgress(0);
        for (Player p: CENTER_LOCATION.getNearbyPlayers(50)){
            bar.addPlayer(p);
        }

        if(countdown){
            this.countdown = 6;
            countdownID = startBeginningCountdown();
        } else {
            setDoorBlocks(Material.AIR);
            tickerId = startTicker();
        }
    }

    public void endEvent(){
        HandlerList.unregisterAll(inEventListener);
        Bukkit.getScheduler().cancelTask(tickerId);
        bar.removeAll();

        resetEvent(true);
    }

    private int countdownID;
    private int countdown = 6;

    public int startBeginningCountdown(){
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(EventMain.getInstance(), () -> {
            countdown--;

            if(countdown < 1){
                setDoorBlocks(Material.AIR);
                tickerId = startTicker();
                stopBeginningCountdown();

                for (Player p: inEvent){
                    p.showTitle(Title.title(Component.text(ChatColor.YELLOW + "Go!"), Component.text("")));
                    p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
                }

            } else {
                for (Player p: ARENA_VICINITY.getPlayersIn()){
                    p.showTitle(Title.title(Component.text(ChatColor.YELLOW + "" + countdown), Component.text("gates open in")));
                }
            }
        }, 0, 20);
    }

    public void stopBeginningCountdown(){
        Bukkit.getScheduler().cancelTask(countdownID);
        countdown = 6;
    }

    public void clearItemAndEffects(String name){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:clear " + name);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect clear " + name);
    }

    public void endEvent(String winner){
        tellPlayersInVicinity(winner + " won the round!");

        addTeamScores(!winner.toLowerCase().contains("blue"));

        endEvent();
    }

    public void addTeamScores(boolean yellowWinner){
        Objective crown = EventMain.getInstance().getServer().getScoreboardManager().getMainScoreboard().getObjective("crown");

        Set<Player> winningTeam = YELLOW_TEAM;
        Set<Player> losingTeam = BLUE_TEAM;

        if(!yellowWinner){
            winningTeam = BLUE_TEAM;
            losingTeam = YELLOW_TEAM;
        }

        for (Player p: winningTeam){
            Score score = crown.getScore(p.getName());
            score.setScore(score.getScore() + 3);

            p.sendMessage(ChatColor.GRAY + "You got" + ChatColor.YELLOW + " 3 points" + ChatColor.GRAY + " for winning!");
        }

        for (Player p: losingTeam){
            Score score = crown.getScore(p.getName());
            score.setScore(score.getScore() + 1);

            p.sendMessage(ChatColor.GRAY + "You got" + ChatColor.YELLOW + " 1 point" + ChatColor.GRAY + " for surviving!");
        }
    }

    public void resetEvent(boolean end){
        setCenterBlocks(Material.STONE);
        setDoorBlocks(Material.CYAN_STAINED_GLASS, Material.YELLOW_STAINED_GLASS);

        winningTeam = 0;
        wasLastNeutral = true;
        amount = 0;
        timeUntilOpen = 200;
        if(end){
            for (Player p: inEvent) {
                p.teleport(EXIT_LOCATION);
                clearItemAndEffects(p.getName());
            }

            createTeams();
            CrownGameCommand.reset();
            inEvent.clear();
            BLUE_TEAM.clear();
            YELLOW_TEAM.clear();
        }
    }

    public void createTeams(){
        yellowTeam.unregister();
        blueTeam.unregister();

        yellowTeam = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("yellowTeam");
        blueTeam = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("blueTeam");

        yellowTeam.setAllowFriendlyFire(false);
        blueTeam.setAllowFriendlyFire(false);
    }

    public void setCenterBlocks(Material mat){
        Location toReset = CENTER_LOCATION.clone();

        toReset.getBlock().setType(mat);
        toReset.add(1, 0, 0).getBlock().setType(mat);
        toReset.add(0, 0, 1).getBlock().setType(mat);
        toReset.add(-1, 0, 0).getBlock().setType(mat);
    }

    public void setDoorBlocks(Material mat){
        setDoorBlocks(mat, mat);
    }

    public void setDoorBlocks(Material blue, Material yellow){
        Location toReset = DOOR_LOCATION.clone();
        toReset.getBlock().setType(blue);
        toReset.add(0, 1, 0).getBlock().setType(blue);
        toReset.add(0, 0, 1).getBlock().setType(blue);
        toReset.add(0, -1, 0).getBlock().setType(blue);
        toReset.add(-47, 0, 0).getBlock().setType(yellow);
        toReset.add(0, 1, 0).getBlock().setType(yellow);
        toReset.add(0, 0, -1).getBlock().setType(yellow);
        toReset.add(0, -1, 0).getBlock().setType(yellow);
    }

    public void removePlayer(Player p){
        p.teleport(EXIT_LOCATION);
        inEvent.remove(p);

        BLUE_TEAM.remove(p);
        YELLOW_TEAM.remove(p);
    }

    public void checkCentralBlocks(){
        byte yellowBlocks = 0;
        byte blueBlocks = 0;
        final byte[] xMod = {0, 1, 0, -1};
        final byte[] zMod = {0, 0, 1, 0};
        Location toCheck = CENTER_LOCATION.clone();

        for(int i = 0; i < 4; i++){
            Material mat = toCheck.add(xMod[i], 0, zMod[i]).getBlock().getType();

            if(mat == Material.AIR || mat == Material.GRAY_WOOL){
                winningTeam = 0;
                return;
            }

            if(mat == Material.CYAN_WOOL) blueBlocks++;
            else if(mat == Material.YELLOW_WOOL) yellowBlocks++;
        }

        if(yellowBlocks == 4) winningTeam = 1;
        else if(blueBlocks == 4) winningTeam = -1;
        else winningTeam = 0;
    }

    public void moveToStartingPositions(){
        resetEvent(false);
        moveFromStartToPrepLocation(YELLOW_ENTRY_BOX, YELLOW_START, true);
        moveFromStartToPrepLocation(BLUE_ENTRY_BOX, BLUE_START, false);
        tellPlayersInVicinity("&eTo your positions!");
    }

    public void moveFromStartToPrepLocation(BoundingBox boundingBox, Location loc, boolean yellow){
        for (Entity e: EVENT_WORLD.getNearbyEntities(boundingBox)){
            if(!(e instanceof Player)) continue;
            Player p = (Player) e;

            if(yellow) YELLOW_TEAM.add(p);
            else BLUE_TEAM.add(p);

            p.teleport(loc);
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            p.setSaturation(20f);

            clearItemAndEffects(p.getName());

            giveItems(p, yellow);
            if(yellow) yellowTeam.addEntry(p.getUniqueId().toString());
            else blueTeam.addEntry(p.getUniqueId().toString());

            inEvent.add(p);
        }
    }

    public ItemStack getCustomPotion(){
        ItemStack stack = new ItemStack(Material.SPLASH_POTION);

        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 0, 0), true);
        meta.setDisplayName(ChatColor.WHITE + "Splash potion of Healing");

        stack.setItemMeta(meta);

        return stack;
    }

    public void giveItems(Player p, boolean yellow){
        Material mat = Material.CYAN_WOOL;
        PlayerInventory pInv = p.getInventory();
        if(yellow) mat = Material.YELLOW_WOOL;

        for(ItemStack i: ITEM_LIST){
            pInv.addItem(i);
        }

        pInv.addItem(new ItemStack(mat, 16));
        pInv.setHelmet(new ItemStack(mat));
        pInv.addItem(getCustomPotion());
    }

    private static byte winningTeam = 0;
    private double amount = 0;
    private boolean wasLastNeutral;
    private short timeUntilOpen = 200;

    public int startTicker(){
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(EventMain.getInstance(), () -> {

            if(timeUntilOpen == 0){
                setCenterBlocks(Material.GRAY_WOOL);
                tellPlayersInVicinity("&eThe middle is now open!");
                timeUntilOpen = -1;
            } else if(timeUntilOpen > 0) timeUntilOpen--;

            if(winningTeam == 0){
                bar.setVisible(false);
                wasLastNeutral = true;
                return;
            } else if(wasLastNeutral){
                wasLastNeutral = false;
                bar.setVisible(true);
                bar.setProgress(0);
                amount = 0;
            }

            if(winningTeam == 1){ //yellow
                bar.setTitle("Yellow");
                bar.setColor(BarColor.YELLOW);
            } else if(winningTeam == -1){ //blue
                bar.setTitle("Blue");
                bar.setColor(BarColor.BLUE);
            }

            amount = amount + 0.02;

            try {
                bar.setProgress(amount);
            } catch (IllegalArgumentException e){ //over 1.0
                String winner = "&bBlue";
                if(winningTeam == 1) winner = "&eYellow";
                endEvent(winner);
            }
        }, 1, 1);
    }

    public boolean checkAllPlayersForItems(){
        boolean toReturn = true;
        int playerAmount = 0;

        for (Entity e: EVENT_WORLD.getNearbyEntities(YELLOW_ENTRY_BOX)){
            if(e.getType() != EntityType.PLAYER) continue;

            Player p = (Player) e;
            if(!p.getInventory().isEmpty()){
                tellPlayersInVicinity(p.getName() + " &7doesn't have an empty inventory!");
                toReturn = false;
            }
            playerAmount++;
        }

        for (Entity e: EVENT_WORLD.getNearbyEntities(BLUE_ENTRY_BOX)){
            if(e.getType() != EntityType.PLAYER) continue;
            Player p = (Player) e;
            if(!p.getInventory().isEmpty()){
                tellPlayersInVicinity(p.getName() + " &7doesn't have an empty inventory!");
                toReturn = false;
            }
            playerAmount++;
        }

        if(playerAmount < 2){
            toReturn = false;

            if(playerAmount == 0) tellPlayersInVicinity("&eNo players are in the starting areas!");
            else tellPlayersInVicinity("&eNot enough players, one more is needed!");
        }
        return toReturn;
    }

    private void logGame(){
        Announcer.log(Level.INFO, "--- Arena round starting ---");
        StringBuilder s = new StringBuilder("Participants, Blue Team: ");
        for (Player p: BLUE_TEAM){
            s.append(p.getName()).append(" ");
        }
        s.append(". Yellow Team: ");
        for (Player p: YELLOW_TEAM){
            s.append(p.getName()).append(" ");
        }
        Announcer.log(Level.INFO, s.toString());
    }
}