package net.forthecrown.events;

import net.forthecrown.core.MiniGameRegion;
import net.forthecrown.commands.CommandVolleyBall;
import org.bukkit.entity.Chicken;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VolleyBallListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Chicken)) return;
        if(!event.getEntity().getPersistentDataContainer().has(CommandVolleyBall.KEY, PersistentDataType.BYTE)) return;

        Chicken chicken = (Chicken) event.getEntity();

        chicken.addPotionEffect(new PotionEffect(
                PotionEffectType.LEVITATION,
                MiniGameRegion.chickenLevitationTime(),
                MiniGameRegion.chickenLevitation(),
                false,
                false,
                false
        ));
        chicken.setFireTicks(0);
    }
}
