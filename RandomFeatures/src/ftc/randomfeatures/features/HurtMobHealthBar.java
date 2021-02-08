package ftc.randomfeatures.features;

import ftc.randomfeatures.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HurtMobHealthBar implements Listener {

    public Set<LivingEntity> delay = new HashSet<>();
    public Map<UUID, Integer> hitmobs = new HashMap<>();


    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent event){
        // Player should be damager:
        if(!(event.getDamager() instanceof Player)) return;

        // Exclude dungeons:
        if(event.getEntity().getWorld() == Bukkit.getWorld("world_void")) return;

        // Entity should be alive, but not player or boss mob:
        if(!(event.getEntity() instanceof LivingEntity)) return;
        if(event.getEntity() instanceof Player) return;
        if(event.getEntity().getType() == EntityType.ENDER_DRAGON || event.getEntity().getType() == EntityType.WITHER) return;

        LivingEntity damaged = (LivingEntity) event.getEntity();

        if(damaged.getHealth() - event.getFinalDamage() <= 0) return;

        String name = damaged.getCustomName();

        // Only affect entities that only show names when player hovers mouse over them:
        // (Note: colored names can get replaced, they return properly anyway)
        if(name != null) {
            if (!name.contains("❤")) {
                if (damaged.isCustomNameVisible()) return; // Don't change names of entities with always visible names (without hearts in them)
                else Main.plugin.withSetNames.put(damaged.getUniqueId(), name); // Save names of player-named entities
            }
        }

        // Calculate hearts to show:
        int maxHealth = (int) Math.ceil(damaged.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() / 2);
        int remainingHRTS = (int) Math.ceil((damaged.getHealth() - event.getFinalDamage()) / 2);
        if (remainingHRTS < 0) remainingHRTS = 0;
        if (remainingHRTS > 20) return;

        int heartsToShow; // Entities with too many hearts, can at max show 20 hearts, if their health is above that, hearts don't show.
        if (maxHealth > 20) heartsToShow = 20;
        else heartsToShow = maxHealth;

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

        UUID id = damaged.getUniqueId();
        if (hitmobs.containsKey(id)) hitmobs.replace(id, hitmobs.get(id) + 1);
        else hitmobs.put(id, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!hitmobs.containsKey(id)) return;
                hitmobs.replace(id, hitmobs.get(id) - 1);

                if(hitmobs.get(id) == 0) {
                    damaged.setCustomNameVisible(false);
                    damaged.setCustomName(Main.plugin.withSetNames.getOrDefault(id, null));
                    Main.plugin.withSetNames.remove(id);
                    hitmobs.remove(id);
                }
            }
        }.runTaskLater(Main.plugin, 5*20);
    }
}