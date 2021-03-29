package net.forthecrown.royals.enchantments;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class HealingBlock extends CrownEnchant implements Listener {

    private final NamespacedKey key;
    private final Plugin plugin;
    private final Set<String> cd = new HashSet<>();

    public HealingBlock(NamespacedKey key, Plugin plugin) {
        super(key);
        this.plugin = plugin;
        this.key = key;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType() == Material.SHIELD;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public @NotNull String getName() {
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
