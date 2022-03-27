package net.forthecrown.dungeons.rewrite_4.component;

import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.rewrite_4.BossComponent;
import net.forthecrown.events.Events;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public abstract class BossEntity extends BossComponent
        implements BossStatusListener, Listener, BossHealthListener
{
    protected Entity entity;
    protected BossHealth health;

    public Entity getEntity() {
        return entity;
    }

    protected abstract Entity summon(BossContext context, World w, Location l);

    @Override
    public void onBossDeath(BossContext context, boolean forced) {
        if(!entity.isDead() && entity != null) {
            entity.remove();
            entity = null;
        }

        Events.unregister(this);
    }

    @Override
    public void onBossSummon(BossContext context) {
        Events.register(this);

        health = getComponent(BossHealth.class);
        entity = summon(context, getWorld(), getSpawnLocation());
        onHealthSet(health);
    }

    @Override
    public void onHealthSet(BossHealth health) {
        if(entity instanceof LivingEntity living) {
            AttributeInstance maxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            DungeonUtils.clearModifiers(maxHealth);

            maxHealth.setBaseValue(health.getMaxHealth());
            living.setHealth(health.getHealth());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if(!event.getEntity().equals(getEntity())) return;

        Entity damager = (event instanceof EntityDamageByEntityEvent e) ? e.getDamager() : null;
        BossHealth.Damage dmg = health.damage(event.getFinalDamage(), damager, event.getCause());

        event.setDamage(dmg.damage);
        event.setCancelled(dmg.cancelled);
    }
}