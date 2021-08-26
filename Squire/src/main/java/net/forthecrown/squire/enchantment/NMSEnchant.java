package net.forthecrown.squire.enchantment;

import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

public class NMSEnchant extends Enchantment {
    private final String name;
    private final RoyalEnchant wrapper;

    public NMSEnchant(String name, RoyalEnchant wrapper, EnchantmentCategory category, EquipmentSlot... slots) {
        super(Rarity.VERY_RARE, category, slots);
        this.name = name;
        this.wrapper = wrapper;
    }

    @Override
    public @NotNull Component getFullname(int level) {
        return PaperAdventure.asVanilla(wrapper.displayName(level));
    }

    public String getName() {
        return name;
    }

    @Override
    public int getMinLevel() {
        return wrapper.getStartLevel();
    }

    @Override
    public int getMaxLevel() {
        return wrapper.getMaxLevel();
    }

    @Override
    public boolean isTreasureOnly() {
        return wrapper.isTreasure();
    }

    @Override
    public boolean isTradeable() {
        return wrapper.isTradeable();
    }

    @Override
    public boolean isDiscoverable() {
        return wrapper.isDiscoverable();
    }

    @Override
    public boolean isCurse() {
        return wrapper.isCursed();
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack itemstack) {
        return wrapper.canEnchantItem(CraftItemStack.asBukkitCopy(itemstack));
    }
}
