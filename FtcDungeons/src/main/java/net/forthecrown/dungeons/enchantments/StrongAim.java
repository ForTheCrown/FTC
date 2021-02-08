package net.forthecrown.dungeons.enchantments;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class StrongAim extends Enchantment implements Listener {

    NamespacedKey key;
    Plugin plugin;

    public StrongAim(NamespacedKey key, Plugin plugin) {
        super(key);
        this.plugin = plugin;
        this.key = key;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (event.getBow().containsEnchantment(this)) {
            if (event.getForce() == 1) {
                if (event.getProjectile() instanceof Arrow) {
                    strongAim((Arrow) event.getProjectile());
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

    private void strongAim(Arrow arrow) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                arrow.setVelocity(arrow.getVelocity().add(new Vector(0, 0.075, 0)));
                if (!arrow.isOnGround()) strongAim(arrow);
            }
        }, 3L);
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        if (item.getType() == Material.BOW) return true;
        else return false;
    }

    @Override
    public boolean conflictsWith(Enchantment arg0) {
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public String getName() {
        return "Strong Aim";
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
