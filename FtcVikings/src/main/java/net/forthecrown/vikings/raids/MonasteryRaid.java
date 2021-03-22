package net.forthecrown.vikings.raids;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.vikings.LiteralLootTable;
import net.forthecrown.vikings.Vikings;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class MonasteryRaid extends VikingRaid {

    /*
     * ---------------------------------
     * Placeholder class, used for testing
     * Delete or rework when done
     * ---------------------------------
     */

    public static final Set<LivingEntity> zombies = new HashSet<>();

    public MonasteryRaid() {
        super(new Location(Bukkit.getWorld("world_void"), -509, 4, -493),
                "Monastery", Vikings.getInstance().getServer());

        World world = Bukkit.getWorld("world_void");

        generator = new RaidAreaGenerator(this)
                .onHostileSpawn(zombie ->{
                    zombie.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
                    zombie.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
                    zombie.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
                    zombie.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
                    //double health = zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * getCurrentParty().getModifier();
                    //zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                    //zombie.setHealth(health);
                    zombies.add(zombie);
                })
                .setHostileMobs(EntityType.ZOMBIE)
                .setHostileLocations(new Location(world, -500, 4, -495))

                .setPassiveMobs(EntityType.VILLAGER, EntityType.ELDER_GUARDIAN)
                .setPassiveLocations(
                        new Location(world, -510, 5, -495),
                        new Location(world, -510, 5, -496)
                )

                .setOriginalArea(new CrownBoundingBox(world, -429, 1, -429, -464, 20, -464))
                .setNewArea(new CrownBoundingBox(world, -478, 1, -478, -514, 20, -514))

                .setChestLootTable(new LiteralLootTable(new NamespacedKey(Vikings.getInstance(), "loottable"),
                        new ItemStack(Material.SHEARS),
                        new ItemStack(Material.DIAMOND, 16),
                        new ItemStack(Material.YELLOW_TERRACOTTA, 42),
                        new ItemStack(Material.YELLOW_BANNER, 10),
                        new ItemStack(Material.YELLOW_CONCRETE, 15),
                        new ItemStack(Material.NETHERITE_SWORD),
                        new ItemStack(Material.ORANGE_TERRACOTTA, 13)))
                .setChestLocations(
                        new Location(world, -510, 42, -495),
                        new Location(world, -510, 42, -496),
                        new Location(world, -510, 42, -494)
                );
    }

    @Override
    public void onEnd() {
        if(zombies.size() > 0) for (Entity z: zombies) z.remove();
    }

    @Override
    public void onComplete() {
        Announcer.ac("Raid complete! :D");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(!(event.getEntity() instanceof Zombie)) return;
        Zombie zombie = (Zombie) event.getEntity();
        if(!zombies.contains(zombie)) return;

        event.getDrops().clear();
        Announcer.ac("You killed a zomzom :D");
        zombies.remove(zombie);

        if(zombies.size() < 1){
            Announcer.ac("Finished raid in event");
            completeRaid();
        }
    }

    private void spawnMobs(){
        for (int i = 0; i < 10; i++){
            Location spawnLoc = getRaidLocation().clone();
            Zombie zombie = (Zombie) getRaidLocation().getWorld().spawnEntity(spawnLoc.add(CrownUtils.getRandomNumberInRange(-10, 10), 0, CrownUtils.getRandomNumberInRange(-10, 10)), EntityType.ZOMBIE);

            zombie.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
            zombie.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
            zombie.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
            zombie.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
            zombie.setHealth(getCurrentParty().getModifier() * 20);
            zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)
                    .setBaseValue(zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() * getCurrentParty().getModifier());
            zombies.add(zombie);
        }
    }
}
