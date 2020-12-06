package ftc.randomfeatures.features;

import ftc.randomfeatures.Main;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HurtMobHealthBar implements Listener {

    public Set<LivingEntity> delay = new HashSet<>();

    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent event){
        if(!(event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) && event.getEntity() instanceof Player) return;
        //if(event.getEntity().getCustomName() != null && !event.getEntity().getCustomName().contains("❤ ")) return;
        if(((LivingEntity) event.getEntity()).getHealth() <= 0) return;

        LivingEntity damaged = (LivingEntity) event.getEntity();

        double maxHealth = damaged.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()/2;
        double currentHealth = damaged.getHealth()/2;
        double healthLost = maxHealth - currentHealth;

        if(damaged.getCustomName() != null && !damaged.getCustomName().contains("❤")) Main.plugin.withSetNames.put(damaged.getUniqueId(), damaged.getCustomName());
        String healthBar = "";

        for(int i = 0; i <= maxHealth; i++){
            if(i <= healthLost) healthBar += ChatColor.GRAY + "❤";
            else healthBar += ChatColor.RED + "❤";
        }
        damaged.setCustomNameVisible(true);
        damaged.setCustomName(healthBar);

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
