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

import java.util.HashSet;
import java.util.Set;

public class HurtMobHealthBar implements Listener {

    public Set<LivingEntity> delay = new HashSet<>();

    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent event){
        if(!(event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) && event.getEntity() instanceof Player) return;
        if(event.getEntity().getWorld() == Bukkit.getWorld("world_void")) return;
        if(!(event.getEntity() instanceof  LivingEntity)) return;
        if(((LivingEntity) event.getEntity()).getHealth() <= event.getFinalDamage()) return;
        if(event.getEntity().getType() == EntityType.ENDER_DRAGON || event.getEntity().getType() == EntityType.WITHER) return;

        if(event.getEntity().getCustomName() != null &&
                event.getEntity().getCustomName().contains(ChatColor.translateAlternateColorCodes('&', event.getEntity().getCustomName())) &&
                !event.getEntity().getCustomName().contains("❤")) return;

        LivingEntity damaged = (LivingEntity) event.getEntity();

        if(damaged.getCustomName() != null && !damaged.getCustomName().contains("❤")) Main.plugin.withSetNames.put(damaged.getUniqueId(), damaged.getCustomName());

        double maxHealth = damaged.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()/2;
        double lostHealth = event.getDamage()/2;
        double remainingHRTS = (damaged.getHealth()/2) - lostHealth;

        if(maxHealth > 20){
            double sizeDifference = maxHealth / 20;
            maxHealth = maxHealth / sizeDifference;
            lostHealth = lostHealth / sizeDifference;
            remainingHRTS = (damaged.getHealth()/2) - lostHealth;
        }

        String healthBar = ChatColor.RED + "";
        for (int i = 0; i < maxHealth; i++){
            if(i == remainingHRTS) healthBar += ChatColor.GRAY + "";
            else healthBar += ChatColor.RED + "";
            healthBar += "❤";
        }

        damaged.setCustomNameVisible(true);
        damaged.setCustomName(healthBar);

        //stops the delay being activated multiple times. Don't know how to make it so if you hit it multiple times it extends the delay length.
        if(delay.contains(damaged)) return;
        delay.add(damaged);

        new BukkitRunnable() {
            @Override
            public void run() {
                damaged.setCustomName(Main.plugin.withSetNames.getOrDefault(damaged.getUniqueId(), null));
                Main.plugin.withSetNames.remove(damaged.getUniqueId());
                damaged.setCustomNameVisible(false);
                delay.remove(damaged);
            }
        }.runTaskLater(Main.plugin, 5*20);
    }
}
