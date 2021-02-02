package net.forthecrown.mazegen;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.BoundingBox;

import javax.annotation.Nullable;
import java.util.*;

public final class Main extends JavaPlugin {

    public static Main plugin;

    private int width;
    private int height;

    private MazeGenerator mazeGenerator;

    public static Location POS_1;
    public static Location POS_2;
    public static Location WAIT_POS;
    public static Location LEADERBOARD_POS;

    public static BoundingBox boundingBox;
    public static Entity exitStand;

    public static MazeTimer timer;
    public Player inMaze;

    public InMazeEvents inMazeEvents;

    @Override
    public void onEnable() {
        plugin = this;

        POS_2 = new Location(Bukkit.getWorld("world_void"), 500, 100, 500);
        POS_1 = new Location(Bukkit.getWorld("world_void"), 565, 100, 565);
        boundingBox = new BoundingBox(POS_2.getX(), POS_2.getY()-1, POS_2.getZ(), POS_1.getX(), POS_1.getY()+6, POS_1.getZ());

        WAIT_POS = new Location(Bukkit.getWorld("world_void"), 494.5, 100, 492.5);
        LEADERBOARD_POS = new Location(Bukkit.getWorld("world_void"), 498.5, 101.5, 492.5);

        getCommand("mazegen").setExecutor(new MazeGenCommand(this));
        getCommand("leavemaze").setExecutor(new LeaveMazeCommand(this));
        getCommand("updatemlb").setExecutor(new LeaderboardUpdate(this));

        Events events = new Events(this);
        inMazeEvents = new InMazeEvents(this);
        getServer().getPluginManager().registerEvents(events, this);

        calculateHeightWidth(POS_1, POS_2);

        timer = new MazeTimer(this);
    }

    @Override
    public void onDisable() {
        if(inMaze != null) inMaze.teleport(WAIT_POS);
        if(exitStand != null) exitStand.remove();
    }


    public void clearLeaderboard(){
        for (Entity e : LEADERBOARD_POS.getNearbyEntities(2, 2, 2)){
            if(e.getType() != EntityType.ARMOR_STAND) continue;
            if(e.getCustomName() == null) continue;

            e.remove();
        }
    }

    public void reloadLeaderboard(){
        clearLeaderboard();

        Location loc = LEADERBOARD_POS.clone();
        Map<String, Integer> scoremap = getTopScores();
        List<String> results = getScoreList(scoremap);

        createLeaderboardStand(loc, ChatColor.GOLD + "---------=o=O=o=---------");
        createLeaderboardStand(loc.subtract(0, 0.25, 0), ChatColor.WHITE + "Leaderboard");

        for (int i = 0; i < 10; i++){
            if(i > results.size()-1) break;
            createLeaderboardStand(loc.subtract(0, 0.25, 0), (i+1) + results.get(i));
        }
        createLeaderboardStand(loc.subtract(0, 0.25, 0), ChatColor.GOLD + "---------=o=O=o=---------");
    }

    public Map<String, Integer> getTopScores(){
        Objective objective = getServer().getScoreboardManager().getMainScoreboard().getObjective("crown");
        Map<String, Integer> tempMap = new HashMap<>();

        for (String s : objective.getScoreboard().getEntries()){
            if(!objective.getScore(s).isScoreSet()) continue;
            if(objective.getScore(s).getScore() < 1) continue;

            tempMap.put(s, objective.getScore(s).getScore());
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(tempMap.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public List<String> getScoreList(Map<String, Integer> map){
        List<String> tempList = new ArrayList<>();

        int asd = 0;
        for (String s : map.keySet()){
            asd++;

            tempList.add(". " + ChatColor.YELLOW + s + ChatColor.WHITE + " - " + org.bukkit.ChatColor.YELLOW + timer.getTimerMessage(map.get(s)));
        }

        return tempList;
    }



    public void createLeaderboardStand(Location loc, String name){
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

        stand.setCustomName(name);
        stand.setCustomNameVisible(true);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setCanMove(false);
    }




    int anInt;
    int id = 0;
    public void enterEvent(Player player){
        inMaze = player;
        anInt = 14;
        id = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            player.sendTitle(ChatColor.of("#FFFFA1") + "" + anInt, ChatColor.GOLD + "Generating maze!", 5, 20, 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2.0f, 2.0f);
            anInt--;

            if(!player.getWorld().getName().contains("world_void") || anInt < 1) Bukkit.getScheduler().cancelTask(id);
        }, 20, 20);

        generateMaze();
        generateIngameMaze(player);
        System.out.println(player.getName() + " entered maze!");
    }

    public void endEvent(Player player){
        inMaze = null;

        player.teleport(WAIT_POS);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        timer.destroyTimer();
        if(player.getInventory().contains(getBomber())) player.getInventory().removeItemAnySlot(getBomber());

        PlayerMoveEvent.getHandlerList().unregister(inMazeEvents);
        PlayerInteractEvent.getHandlerList().unregister(inMazeEvents);
        PlayerQuitEvent.getHandlerList().unregister(inMazeEvents);
        PlayerDeathEvent.getHandlerList().unregister(inMazeEvents);

        exitStand.remove();
        exitStand = null;
    }

    public void finishEvent(Player player){
        endEvent(player);

        Score score = getServer().getScoreboardManager().getMainScoreboard().getObjective("crown").getScore(player.getName());

        String message = ChatColor.GRAY + "You got a score of " + ChatColor.YELLOW + timer.getTimerMessage(timer.getPlayerTime()).toString() + ChatColor.GRAY + "! ";
        if(!score.isScoreSet() || score.getScore() == 0 || score.getScore() > timer.getPlayerTime()){ //better score
            score.setScore(timer.getPlayerTime());
            message += ChatColor.GRAY + "A new record!";
        } else message += ChatColor.GRAY + "Better luck next time!"; //worse score

        reloadLeaderboard();
        player.sendMessage(message);
    }






    public void calculateHeightWidth(Location loc1, Location loc2){
        if(loc1 == null || loc2 == null) return;

        width = loc2.getBlockZ() - loc1.getBlockZ();
        height = loc2.getBlockX() - loc1.getBlockX();

        if(width < 0) width = width * -1;
        if(height < 0) height = height * -1;
    }

    public void generateMaze(){
        calculateHeightWidth(POS_1, POS_2);
        mazeGenerator = new MazeGenerator(width/2, height/2);

        //has to be empty, generate returns false if generation hasn't finished, if it returns true, generation is finished
        while(!mazeGenerator.generate()){ }
        mazeGenerator.draw("CurrentMaze");
    }

    int i = 0;
    int id2;
    public void generateIngameMaze(@Nullable Player toTeleportIn){
        final long startTime = System.currentTimeMillis();
        int size = mazeGenerator.getBitSet().size();

        for (Entity ent : POS_1.getWorld().getNearbyEntities(boundingBox)){
            if(ent.getType() != EntityType.DROPPED_ITEM) continue;
            ent.remove();
        }

        i = 0;
         id2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if(size < i){ //stops the generation when its finished
                Bukkit.getScheduler().cancelTask(id2);

                //when a new maze generates, it sometimes does the stupid thing of dumping old dispensers' contents out into the maze
                //this clears that stuff
                for (Entity ent : POS_1.getWorld().getNearbyEntities(boundingBox)){
                    if(ent.getType() != EntityType.DROPPED_ITEM) continue;
                    if(((Item) ent).getItemStack().getType() != Material.SPLASH_POTION) continue;

                    ent.remove();
                }

                if(toTeleportIn != null){
                    if(!toTeleportIn.getWorld().getName().contains("world_void")){
                        inMaze = null;
                        Bukkit.getScheduler().cancelTask(id2);
                        return;
                    }

                    Location[] startAndEnd = getStartAndEndLocs();
                    toTeleportIn.teleport(startAndEnd[0]);

                    toTeleportIn.sendTitle(ChatColor.of("#FFFFA1") + "Go!!", null, 5, 20, 5);
                    toTeleportIn.playSound(toTeleportIn.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 2.0f, 1.2f);

                    for (PotionEffect effect : toTeleportIn.getActivePotionEffects()) toTeleportIn.removePotionEffect(effect.getType());

                    getServer().getPluginManager().registerEvents(inMazeEvents, this);
                    inMazeEvents.i = 3;
                    timer.startTimer(toTeleportIn);

                    if(toTeleportIn.getInventory().firstEmpty() != -1) toTeleportIn.getInventory().setItem(toTeleportIn.getInventory().firstEmpty(), getBomber());

                    ArmorStand exit = (ArmorStand) startAndEnd[1].getWorld().spawnEntity(startAndEnd[1], EntityType.ARMOR_STAND);
                    exit.setInvulnerable(true);
                    exit.setCanMove(false);
                    exit.getEquipment().setHelmet(new ItemStack(Material.GLOWSTONE, 1));
                    exit.setInvisible(true);
                    exit.setCustomName(ChatColor.YELLOW + "Exit the maze!");
                    exit.setCustomNameVisible(true);

                    if(exitStand != null) exitStand.remove();
                    exitStand = exit;
                }

                final long elapsedTime = (System.currentTimeMillis() - startTime)/1000;

                System.out.println("Ingame Maze generation finished! Elapsed time: " + elapsedTime + " seconds");
                return;
            }

             //I SWEAR, this is the only way to make this generation faster, using a for loop crashes the server everytime, even if it's only a 3 iteration for loop
             // 14 copy pastes xD
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;
             doTheThing(!mazeGenerator.getBitSet().get(i), i);
             i++;

        }, 0, 1);
    }

    public void doTheThing(boolean isWall, int i){
        int x = i % mazeGenerator.getWidth();
        int z = i / mazeGenerator.getWidth();
        Location placeLoc = POS_1.clone().subtract(x, 0, z);

        if(isWall){
            if(placeLoc.getBlock().getType() == Material.AIR || placeLoc.getBlock().getType() == Material.STONE_PRESSURE_PLATE){
                placeLoc.getBlock().setType(getWallMaterial());
                placeLoc.add(0, 1, 0).getBlock().setType(getWallMaterial());
                placeLoc.add(0, 1, 0).getBlock().setType(getWallMaterial());
                placeLoc.add(0, 1, 0).getBlock().setType(getWallMaterial());
            }
        } else {
            if(placeLoc.getBlock().getType() != Material.AIR){
                placeLoc.getBlock().setType(Material.AIR);
                placeLoc.add(0, 1, 0).getBlock().setType(Material.AIR);
                placeLoc.add(0, 1, 0).getBlock().setType(Material.AIR);
                placeLoc.add(0, 1, 0).getBlock().setType(Material.AIR);
                placeLoc.subtract(0, 3, 0);
            }

            placeLoc.subtract(0, 1, 0);
            if(placeLoc.getBlock().getType() == Material.DISPENSER || placeLoc.getBlock().getType() == Material.DIORITE){
                if(placeLoc.getBlock().getState() instanceof Dispenser) ((Dispenser) placeLoc.getBlock().getState()).getInventory().clear();
                placeLoc.getBlock().setType(Material.ANDESITE, false);
            }
            placeLoc.add(0, 1, 0);

            if(getRandomNumberInRange(0, 500) <= 10){
                if(getRandomNumberInRange(0, 4) == 1) placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), new ItemStack(Material.DIAMOND, 1));
                else placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), new ItemStack(Material.GOLD_INGOT, 1));

            } else if(getRandomNumberInRange(0, 500) <= 10){
                placeLoc = POS_1.clone().subtract(x, 0, z);

                placeLoc = placeLoc.subtract(0, 1, 0);
                placeLoc.getBlock().setType(Material.AIR);
                placeLoc.getBlock().setType(Material.DISPENSER,false);

                Dispenser dispenser = (Dispenser) placeLoc.getBlock().getState();

                org.bukkit.block.data.type.Dispenser dispenser1 = (org.bukkit.block.data.type.Dispenser) dispenser.getBlockData();
                dispenser1.setFacing(BlockFace.UP);
                dispenser.setBlockData(dispenser1);

                dispenser.getSnapshotInventory().addItem(getCustomPotion());
                dispenser.update();

                placeLoc.add(0, 1, 0).getBlock().setType(Material.STONE_PRESSURE_PLATE, false);
            }
        }
    }






    public ItemStack getCustomPotion(){
        ItemStack stack = new ItemStack(Material.SPLASH_POTION, 16);

        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 15*20, 0), true);

        stack.setItemMeta(meta);

        return stack;
    }

    public ItemStack getBomber(){
        return makeItem(Material.FIREWORK_STAR, 1, true, "&eBomb", "&bUse this on a wall to destroy it", "&73 uses total");
    }

    public static ItemStack makeItem(Material material, int amount, boolean hideFlags, String name, String... loreStrings) {
        ItemStack result = new ItemStack(material, amount);
        ItemMeta meta = result.getItemMeta();

        if (name != null) meta.setDisplayName(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', name));
        if (loreStrings != null) {
            List<String> lore = new ArrayList<>();
            for(String s : loreStrings){ lore.add(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', s)); }
            meta.setLore(lore);
        }
        if (hideFlags) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }

        result.setItemMeta(meta);
        return result;
    }

    public Location[] getStartAndEndLocs(){
        Location start;
        Location end;
        Location[] toReturn = new Location[2];

        if(POS_1.getBlockZ() > POS_2.getBlockZ()){
            end = POS_1.clone();
            start = POS_2.clone().add(0, 0, 1);
        } else {
            end = POS_2.clone();
            start = POS_1.clone().add(0, 0, 1);
        }

        //find start
        for (int i = 0; i < width; i++){
            if(toReturn[0] == null){
                start = start.add(1, 0, 0);
                if(start.getBlock().getType() == Material.AIR || start.getBlock().getType() == Material.STONE_PRESSURE_PLATE) toReturn[0] = start.add(0.5, 0, 0.5);
            }

            if(toReturn[1] == null){
                end = end.subtract(1, 0, 0);
                if(end.getBlock().getType() == Material.AIR || end.getBlock().getType() == Material.STONE_PRESSURE_PLATE)toReturn[1] = end.add(0.5, 0, 0.5);
            }
            if(toReturn[0] != null && toReturn[1] != null) break;
        }
        return toReturn;
    }

    //REMINDER: Andesite is floor material and Stone surrounds entire area, preventing escaping with bombs

    public Material getWallMaterial(){
        switch (getRandomNumberInRange(0, 3)){
            default: return Material.COBBLESTONE;
            case 1: return Material.STONE_BRICKS;
            case 2: return Material.CRACKED_STONE_BRICKS;
            case 3: return Material.MOSSY_STONE_BRICKS;
        }
    }

    public List<Material> getDestroyableSurfaces(){
        return Arrays.asList(Material.COBBLESTONE, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
    }

    public Integer getRandomNumberInRange(int min, int max) {
        if (min >= max) return 0;
        return new Random().nextInt((max - min) + 1) + min;
    }

}
