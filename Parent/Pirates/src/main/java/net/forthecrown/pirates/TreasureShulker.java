package net.forthecrown.pirates;

import net.forthecrown.core.utils.CrownUtils;
import org.bukkit.*;
import org.bukkit.entity.Shulker;
import org.bukkit.persistence.PersistentDataType;

public class TreasureShulker {

    private final Pirates main;
    public static final NamespacedKey KEY = new NamespacedKey(Pirates.inst, "treasure");

    public TreasureShulker(Pirates pirates){
        main = pirates;
    }

    public Location findRandLocation(){
        final int x = CrownUtils.getRandomNumberInRange(-1970, 1970);
        final int y = CrownUtils.getRandomNumberInRange(40, 50);
        final int z = CrownUtils.getRandomNumberInRange(-1970, 1970);

        main.getConfig().set("TreasureLoc.x", x);
        main.getConfig().set("TreasureLoc.y", y);
        main.getConfig().set("TreasureLoc.z", z);

        return new Location(Bukkit.getWorld(main.getConfig().getString("TreasureLoc.world")), x, y, z);
    }

    public void spawn(){
        Location spawnLoc = findRandLocation();
        spawnLoc.getWorld().spawn(spawnLoc, Shulker.class, shulker -> {
            shulker.setAI(false);
            shulker.setInvulnerable(true);
            shulker.setColor(DyeColor.GRAY);
            shulker.setRemoveWhenFarAway(false);
            shulker.setPersistent(true);
            shulker.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
        });
    }

    public void killOld(){
        Location location = new Location(Bukkit.getWorld(main.getConfig().getString("TreasureLoc.world")), main.getConfig().getInt("TreasureLoc.x"), main.getConfig().getInt("TreasureLoc.y"), main.getConfig().getInt("TreasureLoc.z"));

        for (Shulker s: location.getNearbyEntitiesByType(Shulker.class, 1)){
            if(!s.getPersistentDataContainer().has(KEY, PersistentDataType.BYTE)) continue;
            s.remove();
        }
    }
}
