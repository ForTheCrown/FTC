package net.forthecrown.royals.enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DolphinSwimmer extends CrownEnchant implements Listener {

    private final NamespacedKey key;

    public DolphinSwimmer(NamespacedKey key, Plugin plugin) {
        super(key);
        this.key = key;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return getItemTarget().includes(item.getType());
    }

    @Override
    public boolean conflictsWith(Enchantment arg0) {
        return false;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TRIDENT;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public @NotNull String getName() {
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
