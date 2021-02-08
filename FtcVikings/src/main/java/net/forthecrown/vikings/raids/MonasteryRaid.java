package net.forthecrown.vikings.raids;

import net.forthecrown.core.FtcCore;
import net.forthecrown.vikings.raids.managers.GenericRaid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class MonasteryRaid extends GenericRaid {

    /*
     * ---------------------------------
     * Placeholder class, used for testing
     * Delete when or rework when done
     * ---------------------------------
     */

    public static final Set<Zombie> zombies = new HashSet<>();

    public MonasteryRaid() {
        super(new Location(Bukkit.getWorld("world_void"), -200, 6, -200), "Monastery");
    }

    @Override
    public void onRaidLoad() {
        if(zombies.size() > 0) for (Zombie z : zombies) z.remove();
        zombies.clear();

        spawnMobs();
    }

    @Override
    public void onRaidComplete() {
        Bukkit.broadcastMessage("Raid complete! :D");

        super.onRaidEnd();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(!(event.getEntity() instanceof Zombie)) return;
        Zombie zombie = (Zombie) event.getEntity();
        if(!zombies.contains(zombie)) return;

        event.getDrops().clear();
        Bukkit.broadcastMessage("You killed a zomzom :D");
    }

    private void spawnMobs(){
        for (int i = 0; i < 10; i++){
            Location spawnLoc = getRaidLocation().clone();
            Zombie zombie = (Zombie) getRaidLocation().getWorld().spawnEntity(spawnLoc.add(FtcCore.getRandomNumberInRange(1, 5), 0, FtcCore.getRandomNumberInRange(1, 5)), EntityType.ZOMBIE);

            zombie.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
            zombie.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
            zombie.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
            zombie.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
            zombie.setHealth(getDifficulty().getModifier() * 20);
            zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() * getDifficulty().getModifier());
            zombies.add(zombie);
        }
    }


}
