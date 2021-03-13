package net.forthecrown.dungeons.enchantments;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DolphinSwimmer extends Enchantment implements Listener {

    NamespacedKey key;
    Plugin plugin;

    public DolphinSwimmer(NamespacedKey key, Plugin plugin) {
        super(key);
        this.plugin = plugin;
        this.key = key;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onActivate(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getPlayer().isSwimming() && event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(this)) {
                if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta() instanceof Damageable) {
                    Damageable dmg = ((Damageable) event.getPlayer().getInventory().getItemInMainHand().getItemMeta());
                    if (dmg.getDamage() >= 249) return;
                    dmg.setDamage(dmg.getDamage() + 2);

                    event.getPlayer().getInventory().getItemInMainHand().setItemMeta((ItemMeta) dmg);
                }

                event.getPlayer().removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 120, 1));
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1f, 1.5f);
            }
        }
    }

    @EventHandler
    public void anvil(PrepareAnvilEvent event) {
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
        if (item.getType() == Material.TRIDENT) return true;
        else return false;
    }

    @Override
    public @NotNull Component displayName(int level) {
        return Component.text("Dolphin Swimmer");
    }

    @Override
    public boolean conflictsWith(Enchantment arg0) {
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TRIDENT;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public String getName() {
        return "Dolphin Swimmer";
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
