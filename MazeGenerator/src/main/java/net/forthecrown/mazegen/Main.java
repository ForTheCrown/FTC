package net.forthecrown.mazegen;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private int width;
    private int height;
    private Events events;

    private MazeGenerator mazeGenerator;

    public Location pos1;
    public Location pos2;
    public Location waitPos;

    public UUID lastMazeExit = null;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        events = new Events(this);

        getServer().getPluginCommand("mazegen").setExecutor(new MazeGenCommand(this));

        getServer().getPluginManager().registerEvents(events, this);

        pos1 = getConfig().getLocation("MazePos.Pos1");
        pos2 = getConfig().getLocation("MazePos.Pos2");
        waitPos = getConfig().getLocation("EntryPos");

        calculateHeightWidth(pos1, pos2);
    }

    @Override
    public void onDisable() {
        if(events.inMaze.size() > 0){
            for (Player p : events.inMaze){
                p.teleport(new Location(Bukkit.getWorld("world"), 999.5, 70, 199.5));
            }
        }

        getConfig().set("MazePos.Pos1", pos1);
        getConfig().set("MazePos.Pos2", pos2);
        getConfig().set("EntryPos", waitPos);
        saveConfig();
    }

    boolean trash = false;
    public void enterEvent(Player player){
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30*20, 1));
        player.sendTitle("Generating Maze!", "Please wait 30 seconds", 15, 30*20, 15);
        trash = false;
        calculateHeightWidth(pos1, pos2);
        generateMaze();
        generateIngameMaze(player);
    }

    public void calculateHeightWidth(Location loc1, Location loc2){
        if(loc1 == null || loc2 == null) return;

        width = loc2.getBlockZ() - loc1.getBlockZ();
        height = loc2.getBlockX() - loc1.getBlockX();

        if(width < 0) width = width * -1;
        if(height < 0) height = height * -1;
    }

    public void generateMaze(){
        mazeGenerator = new MazeGenerator(width/2, height/2);

        long startTime = System.currentTimeMillis();
        System.out.println("Maze generation starting at: " + startTime);

        while(!mazeGenerator.generate()){ }

        System.out.println("Maze generation finished! Elapsed time: " + ((System.currentTimeMillis() - startTime)) + " milliseconds");
        //generateIngameMaze();
        mazeGenerator.draw("bruh");
    }

    int i = 0;
    Location placeLoc = null;
    long amountOfBlocks = 0;
    public void generateIngameMaze(@Nullable Player toTeleportIn){

        final long startTime = System.currentTimeMillis();
        amountOfBlocks = 0;

        String command = "fill " + pos1.getBlockX() + " " + (pos1.getBlockY()+2) + " " + pos1.getBlockZ() + " " +
                pos2.getBlockX() + " " + pos2.getBlockY() + " " + pos2.getBlockZ() + " air replace";

        String command1 = "fill " + pos1.getBlockX() + " " + (pos1.getBlockY()-1) + " " + pos1.getBlockZ() + " " +
                pos2.getBlockX() + " " + (pos2.getBlockY()-1) + " " + pos2.getBlockZ() + " cobblestone replace";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command1);

        int size = mazeGenerator.getBitSet().size();

        i = 0;
         Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if(size < i){ //stops the generation when its finished

                if(toTeleportIn != null){
                    Location[] startAndEnd = getStartAndEndLocs();

                    toTeleportIn.teleport(startAndEnd[0]);

                    if(lastMazeExit != null) Bukkit.getEntity(lastMazeExit).remove();

                    ArmorStand exit = (ArmorStand) startAndEnd[1].getWorld().spawnEntity(startAndEnd[1], EntityType.ARMOR_STAND);
                    exit.setInvulnerable(true);
                    exit.setCanMove(false);
                    exit.getEquipment().setHelmet(new ItemStack(Material.GLOWSTONE, 1));
                    exit.setInvisible(true);
                    exit.setCustomName(ChatColor.YELLOW + "Exit the maze!");
                    exit.setCustomNameVisible(true);
                    lastMazeExit = exit.getUniqueId();

                    //TODO add keys or collectibles to the maze to increas the amount of points you can get
                }

                final long elapsedTime = (System.currentTimeMillis() - startTime)/1000;
                final long blocksPerSecond = amountOfBlocks / elapsedTime;

                System.out.println("Ingame Maze generation finished! Elapsed time " + elapsedTime + " seconds");
                System.out.println("Blocks placed per second: " + blocksPerSecond);
                Bukkit.getScheduler().cancelTasks(this);
                return;
            }

             int x;
             int z;

             //I SWEAR, this is the only way to make this generation faster, using a for loop crashes the server everytime, even if it's only a 3 iteration for loop
             // 9 copy pastes xD
             if(!mazeGenerator.getBitSet().get(i)){
                 x = i % mazeGenerator.getWidth();
                 z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());
                 amountOfBlocks +=3;
             } else {
                 if(getRandomNumberInRange(0, 100) <= 10){
                     x = i % mazeGenerator.getWidth();
                     z = i / mazeGenerator.getWidth();
                     placeLoc = pos1.clone().subtract(x, 0, z);
                     placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), getGemItem()).setGravity(false);
                 }
             }

             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 x = i % mazeGenerator.getWidth();
                 z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());
                 amountOfBlocks +=3;
             } else {
                 if(getRandomNumberInRange(0, 100) <= 10){
                     x = i % mazeGenerator.getWidth();
                     z = i / mazeGenerator.getWidth();
                     placeLoc = pos1.clone().subtract(x, 0, z);
                     placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), getGemItem()).setGravity(false);
                 }
             }

             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 x = i % mazeGenerator.getWidth();
                 z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());
                 amountOfBlocks +=3;
             } else {
                 if(getRandomNumberInRange(0, 100) <= 10){
                     x = i % mazeGenerator.getWidth();
                     z = i / mazeGenerator.getWidth();
                     placeLoc = pos1.clone().subtract(x, 0, z);
                     placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), getGemItem()).setGravity(false);
                 }
             }

             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 x = i % mazeGenerator.getWidth();
                 z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());
                 amountOfBlocks +=3;
             } else {
                 if(getRandomNumberInRange(0, 100) <= 10){
                     x = i % mazeGenerator.getWidth();
                     z = i / mazeGenerator.getWidth();
                     placeLoc = pos1.clone().subtract(x, 0, z);
                     placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), getGemItem()).setGravity(false);
                 }
             }

             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 x = i % mazeGenerator.getWidth();
                 z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());
                 amountOfBlocks +=3;
             } else {
                 if(getRandomNumberInRange(0, 100) <= 10){
                     x = i % mazeGenerator.getWidth();
                     z = i / mazeGenerator.getWidth();
                     placeLoc = pos1.clone().subtract(x, 0, z);
                     placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), getGemItem()).setGravity(false);
                 }
             }

             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 x = i % mazeGenerator.getWidth();
                 z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());
                 amountOfBlocks +=3;
             } else {
                 if(getRandomNumberInRange(0, 100) <= 10){
                     x = i % mazeGenerator.getWidth();
                     z = i / mazeGenerator.getWidth();
                     placeLoc = pos1.clone().subtract(x, 0, z);
                     placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), getGemItem()).setGravity(false);
                 }
             }

             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 x = i % mazeGenerator.getWidth();
                 z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());
                 amountOfBlocks +=3;
             } else {
                 if(getRandomNumberInRange(0, 100) <= 10){
                     x = i % mazeGenerator.getWidth();
                     z = i / mazeGenerator.getWidth();
                     placeLoc = pos1.clone().subtract(x, 0, z);
                     placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), getGemItem()).setGravity(false);
                 }
             }

             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 x = i % mazeGenerator.getWidth();
                 z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());
                 amountOfBlocks +=3;
             } else {
                 if(getRandomNumberInRange(0, 1000) <= 10){
                     x = i % mazeGenerator.getWidth();
                     z = i / mazeGenerator.getWidth();
                     placeLoc = pos1.clone().subtract(x, 0, z);
                     placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), getGemItem()).setGravity(false);
                 }
             }

             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 x = i % mazeGenerator.getWidth();
                 z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());

                 placeLoc = placeLoc.add(0, 1, 0);
                 placeLoc.getBlock().setType(getWallMaterial());
                 amountOfBlocks +=3;
             } else {
                 if(getRandomNumberInRange(0, 10000) <= 10){
                     x = i % mazeGenerator.getWidth();
                     z = i / mazeGenerator.getWidth();
                     placeLoc = pos1.clone().subtract(x, 0, z);
                     placeLoc.getWorld().dropItem(placeLoc.add(0.5, 1, 0.5), getGemItem()).setGravity(false);
                 }
             }

             i++;
        }, 0, 1);
    }

    public Location[] getStartAndEndLocs(){
        Location start;
        Location end;
        Location[] toReturn = new Location[2];

        if(pos1.getBlockZ() > pos2.getBlockZ()){
            end = pos1.clone();
            start = pos2.clone();
        } else {
            end = pos2.clone();
            start = pos1.clone();
        }

        //find start
        for (int i = 0; i < width; i++){
            if(toReturn[0] == null){
                start = start.add(1, 0, 0);
                if(start.getBlock().getType() == Material.AIR) toReturn[0] = start.add(0.5, 0, 0.5);
            }

            if(toReturn[1] == null){
                end = end.subtract(1, 0, 0);
                if(end.getBlock().getType() == Material.AIR)toReturn[1] = end.add(0.5, 0, 0.5);
            }

            if(toReturn[0] != null && toReturn[1] != null) break;
        }
        return toReturn;
    }

    public Material getWallMaterial(){
        switch (getRandomNumberInRange(0, 5)){
            default: return Material.STONE;
            case 1: return Material.COBBLESTONE;
            case 2: return Material.ANDESITE;
            case 3: return Material.STONE_BRICKS;
            case 4: return Material.CRACKED_STONE_BRICKS;
            case 5: return Material.MOSSY_STONE_BRICKS;
        }
    }

    public ItemStack getGemItem(){
        ItemStack toReturn = new ItemStack(Material.DIAMOND, 1);
        toReturn.addUnsafeEnchantment(Enchantment.LUCK, 1);

        toReturn.getLore().set(0, "A gem from the maze!");
        toReturn.getItemMeta().setDisplayName("Coins collected:");
        return toReturn;
    }

    public Integer getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            return 0;
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

}
