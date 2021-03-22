package net.forthecrown.dungeons.enchantments;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HealingBlock extends Enchantment implements Listener {

    NamespacedKey key;
    Plugin plugin;
    Set<String> cd = new HashSet<>();

    public HealingBlock(NamespacedKey key, Plugin plugin) {
        super(key);
        this.plugin = plugin;
        this.key = key;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.isBlocking() && event.getFinalDamage() == 0) {
                if (player.getInventory().getItemInOffHand().containsEnchantment(this) || player.getInventory().getItemInMainHand().containsEnchantment(this)) {
                    if (!cd.contains(player.getName())) {
                        try {
                            player.setHealth((int) player.getHealth()+2);
                        } catch (IllegalArgumentException e) {
                            player.setHealth(player.getMaxHealth());
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.6f, 1f);
                        cd.add(player.getName());
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                cd.remove(player.getName());
                            }
                        }, 60l);
                    }
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
        if (item.getType() == Material.SHIELD) return true;
        else return false;
    }

    @Override
    public @NotNull Component displayName(int level) {
        return Component.text("Healing Block");
    }

    @Override
    public boolean conflictsWith(Enchantment arg0) {
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public String getName() {
        return "Healing Block";
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
