package net.forthecrown.dungeons.enchantments;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PoisonCrit extends Enchantment implements Listener {

    NamespacedKey key;

    public PoisonCrit(NamespacedKey key, Plugin plugin) {
        super(key);
        this.key = key;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (event.getCause() == EntityDamageEvent.DamageCause.THORNS || event.isCancelled()) return;
            if (player.getInventory().getItemInMainHand().containsEnchantment(this)) {
                if (event.getEntity() instanceof Player) {
                    if (event.getFinalDamage() == 0 && ((Player) event.getEntity()).isBlocking() == true && (((Player) event.getEntity()).getInventory().getItemInOffHand().getType() == Material.SHIELD || ((Player) event.getEntity()).getInventory().getItemInMainHand().getType() == Material.SHIELD)) return;
                }
                @SuppressWarnings("deprecation")
                boolean flag = player.getFallDistance() > 0.0F && !player.isOnGround() && !player.hasPotionEffect(PotionEffectType.BLINDNESS) && event.getEntity() instanceof LivingEntity;
                if (flag) {
                    LivingEntity hitEntity = (LivingEntity) event.getEntity();
                    if (hitEntity instanceof Monster) hitEntity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 45, 2, false, false));
                    else hitEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 35, 1, false, false));

                    Particle.DustOptions dust = new Particle.DustOptions(
                            Color.fromRGB(31, 135, 62), 1);
                    hitEntity.getWorld().spawnParticle(Particle.REDSTONE, hitEntity.getLocation().getX(), hitEntity.getLocation().getY()+(hitEntity.getHeight()/2), hitEntity.getLocation().getZ(), 10, 0.2D, 0.2D, 0.2D, dust);
                    hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_SPIDER_HURT, 0.2f, 0.7f);
                }
            }
        }
    }

    @EventHandler
    public void anvil(PrepareAnvilEvent event) {
        if (event.getInventory() == null) return;
        if (event.getInventory().getItem(0) != null && event.getInventory().getItem(1) != null && event.getInventory().getItem(2) != null)
        {
            if (event.getInventory().getItem(0).containsEnchantment(this) || event.getInventory().getItem(1).containsEnchantment(this))
            {
                ItemStack item = event.getResult();
                if (item == null) return;
                item.addUnsafeEnchantment(this, 1);
                ItemMeta meta = item.getItemMeta();
                ArrayList<String> lore = new ArrayList<String>();
                lore.add(ChatColor.GRAY + this.getName());
                meta.setLore(lore);
                item.setItemMeta(meta);
                event.setResult(item);
            }
        }
    }


    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return false;
    }

    @Override
    public @NotNull Component displayName(int level) {
        return Component.text("Poison Crit");
    }

    @Override
    public boolean conflictsWith(Enchantment ench) {
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public String getName() {
        return "Critical Poison";
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }
}
