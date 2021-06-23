package net.forthecrown.pirates;

import net.forthecrown.utils.CrownRandom;
import org.bukkit.*;
import org.bukkit.entity.Shulker;
import org.bukkit.persistence.PersistentDataType;

public class TreasureShulker {

    private final Pirates main;
    public static final NamespacedKey KEY = new NamespacedKey(Pirates.inst, "treasure");
    private final CrownRandom random;

    public TreasureShulker(Pirates pirates){
        main = pirates;
        random = new CrownRandom();
    }

    public Location findRandLocation(){
        int x = random.intInRange(250, 1970);
        int y = random.intInRange(40, 50);
        int z = random.intInRange(250, 1970);

        if(random.nextBoolean()) x = -x;
        if(random.nextBoolean()) z = -z;

        main.getConfig().set("TreasureLoc.x", x);
        main.getConfig().set("TreasureLoc.y", y);
        main.getConfig().set("TreasureLoc.z", z);
        main.saveConfig();

        return new Location(Bukkit.getWorld(main.getConfig().getString("TreasureLoc.world")), x, y, z);
    }

    public void spawn(){
        Location spawnLoc = findRandLocation();
        spawnLoc.getBlock().setType(Material.AIR);
        spawnLoc.getWorld().spawn(spawnLoc, Shulker.class, shulker -> {
            shulker.setAI(false);
            shulker.setInvulnerable(true);
            shulker.setColor(DyeColor.GRAY);
            shulker.setRemoveWhenFarAway(false);
            shulker.setPersistent(true);
            shulker.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
        });
    }

    public void relocate(){
        killOld();
        spawn();
    }

    public void killOld(){
        Location location = new Location(Bukkit.getWorld(main.getConfig().getString("TreasureLoc.world")), main.getConfig().getInt("TreasureLoc.x"), main.getConfig().getInt("TreasureLoc.y"), main.getConfig().getInt("TreasureLoc.z"));

        for (Shulker s: location.getNearbyEntitiesByType(Shulker.class, 1)){
            if(!s.getPersistentDataContainer().has(KEY, PersistentDataType.BYTE)) continue;
            s.remove();
        }
    }
}
