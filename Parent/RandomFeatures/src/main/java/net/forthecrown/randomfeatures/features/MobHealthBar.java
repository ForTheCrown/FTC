package net.forthecrown.randomfeatures.features;

import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.randomfeatures.RandomFeatures;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class MobHealthBar implements Listener {
    public Map<LivingEntity, BukkitRunnable> hitmobs = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onMobDamage(EntityDamageByEntityEvent event){
        if(!(event.getDamager() instanceof Player)) return;                     //Damager gotta be player
        if(event.getEntity().getWorld().equals(CrownUtils.WORLD_VOID)) return;  //Not in world_void
        if(!(event.getEntity() instanceof LivingEntity)) return;                //Must be alive
        if(event.getEntity() instanceof Player                                  //But not another player, armor stand or Boss mob
                || event.getEntity() instanceof ArmorStand
                || event.getEntity() instanceof EnderDragon
                || event.getEntity() instanceof Wither
        ) return;

        LivingEntity damaged = (LivingEntity) event.getEntity();

        if(damaged.getHealth() - event.getFinalDamage() <= 0) return;

        String name = damaged.getCustomName();

        // Only affect entities that only show names when player hovers mouse over them:
        // (Note: colored names can get replaced, they return properly anyway)
        if(name != null) {
            if (!name.contains("❤")) {
                if (damaged.isCustomNameVisible()) return; // Don't change names of entities with always visible names (without hearts in them)
                else RandomFeatures.instance.withSetNames.put(damaged, damaged.customName()); // Save names of player-named entities
            }
        }

        // Calculate hearts to show:
        int maxHealth = (int) Math.ceil(damaged.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() / 2);
        int remainingHRTS = (int) Math.ceil((damaged.getHealth() - event.getFinalDamage()) / 2);
        if (remainingHRTS < 0) remainingHRTS = 0;
        if (remainingHRTS > 20) return;

        int heartsToShow = Math.min(maxHealth, 20); // Entities with too many hearts, can at max show 20 hearts, if their health is above that, hearts don't show.

        // Construct name with correct hearts:
        String healthBar = ChatColor.RED + "";
        for (int i = 0; i < remainingHRTS; i++){
            healthBar += "❤";
        }
        healthBar += ChatColor.GRAY + "";
        for (int i = remainingHRTS; i < heartsToShow; i++){
            healthBar += "❤";
        }

        // Show hearts + set timer to remove hearts
        // (By using a map<uuid,int>, we can make the delayed event only remove the name when the int -> 0)
        damaged.setCustomNameVisible(true);
        damaged.setCustomName(healthBar);
        delay(damaged);
    }

    private void delay(LivingEntity damaged){
        if(hitmobs.containsKey(damaged)) hitmobs.get(damaged).cancel(); //Cancel if already in map
        BukkitRunnable runnable = new BukkitRunnable() { //Create new delay
            @Override
            public void run() {
                damaged.setCustomNameVisible(false);
                damaged.customName(RandomFeatures.instance.withSetNames.getOrDefault(damaged, null));
                RandomFeatures.instance.withSetNames.remove(damaged);
            }
        };
        runnable.runTaskLater(RandomFeatures.instance, 5*20); //Start new delay
        hitmobs.put(damaged, runnable); //Put delay in map
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!event.getDeathMessage().contains("❤")) return;
        if(!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
        String name = CrownUtils.normalEnum(damageEvent.getDamager().getType());

        String message = event.getDeathMessage().replaceAll("❤", "") + name;
        event.setDeathMessage(message);
    }
}
