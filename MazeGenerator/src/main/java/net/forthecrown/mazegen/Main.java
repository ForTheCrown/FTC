package net.forthecrown.mazegen;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public final class Main extends JavaPlugin {

    private int width;
    private int height;
    private int area;
    private Events events;

    private MazeGenerator mazeGenerator;

    public Location pos1;
    public Location pos2;
    public Location entryLoc;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        events = new Events(this);

        getServer().getPluginCommand("mazegen").setExecutor(new MazeGenCommand(this));

        getServer().getPluginManager().registerEvents(events, this);

        pos1 = getConfig().getLocation("MazePos.Pos1");
        pos2 = getConfig().getLocation("MazePos.Pos2");
        entryLoc = getConfig().getLocation("EntryPos");

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
        getConfig().set("EntryPos", entryLoc);
        saveConfig();
    }

    boolean trash = false;
    int fiveSecond = 5;
    public void enterEvent(Player player){
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5*20, 1));
        player.teleport(entryLoc);
        trash = false;
        calculateHeightWidth(pos1, pos2);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if(fiveSecond <= 0) return;

            player.sendTitle(String.valueOf(fiveSecond), "Generating maze", 0, 20, 20);

            if(!trash){
                generateMaze();
                trash = true;
            }
            fiveSecond--;
        }, 0, 20);
    }

    public void calculateHeightWidth(Location loc1, Location loc2){
        if(loc1 == null || loc2 == null) return;

        width = loc2.getBlockZ() - loc1.getBlockZ();
        height = loc2.getBlockX() - loc1.getBlockX();

        Bukkit.broadcastMessage(width + " W");
        Bukkit.broadcastMessage(height + " H");

        if(width < 0) width = width * -1;
        if(height < 0) height = height * -1;

        Bukkit.broadcastMessage(width + " W");
        Bukkit.broadcastMessage(height + " H");

        area = width * height;
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
    int id;
    public void generateIngameMaze(){

        final long startTime = System.currentTimeMillis();
        amountOfBlocks = 0;

        System.out.println("Ingame Maze generation starting at" + startTime);

        i = 0;
         id = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if(mazeGenerator.getBitSet().size() < i){

                //TODO add clone command to make maze t a l l

                final long elapsedTime = (System.currentTimeMillis() - startTime)/1000;
                final long blocksPerSecond = amountOfBlocks / elapsedTime;

                System.out.println("Ingame Maze generation finished! Elapsed time " + elapsedTime + " seconds");
                System.out.println("Blocks placed per second: " + blocksPerSecond);
                Bukkit.getScheduler().cancelTasks(this);
                return;
            }

             if(!mazeGenerator.getBitSet().get(i)){
                 int x = i % mazeGenerator.getWidth();
                 int z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);

                 placeLoc.getBlock().setType(Material.STONE);
                 amountOfBlocks++;
             }
             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 int x = i % mazeGenerator.getWidth();
                 int z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);

                 placeLoc.getBlock().setType(Material.STONE);
                 amountOfBlocks++;
             }
             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 int x = i % mazeGenerator.getWidth();
                 int z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);

                 placeLoc.getBlock().setType(Material.STONE);
                 amountOfBlocks++;
             }
             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 int x = i % mazeGenerator.getWidth();
                 int z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);

                 placeLoc.getBlock().setType(Material.STONE);
                 amountOfBlocks++;
             }
             i++;
             if(!mazeGenerator.getBitSet().get(i)){
                 int x = i % mazeGenerator.getWidth();
                 int z = i / mazeGenerator.getWidth();
                 placeLoc = pos1.clone().subtract(x, 0, z);

                 placeLoc.getBlock().setType(Material.STONE);
                 amountOfBlocks++;
             }
             i++;
        }, 0, 1);
    }
}
