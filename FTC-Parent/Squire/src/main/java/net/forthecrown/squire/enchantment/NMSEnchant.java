package net.forthecrown.squire.enchantment;

import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;

public class NMSEnchant extends Enchantment {
    private final String name;
    private final RoyalEnchant wrapper;

    public NMSEnchant(String name, RoyalEnchant wrapper, EnchantmentSlotType type, EnumItemSlot... slots) {
        super(Rarity.RARE, type, slots);
        this.name = name;
        this.wrapper = wrapper;
    }

    @Override
    public IChatBaseComponent d(int i) {
        return PaperAdventure.asVanilla(wrapper.displayName(i));
    }

    public String getName() {
        return name;
    }

    @Override
    public int getStartLevel() {
        return wrapper.getStartLevel();
    }

    @Override
    public int getMaxLevel() {
        return wrapper.getMaxLevel();
    }

    @Override
    public boolean isTreasure() {
        return wrapper.isTreasure();
    }

    @Override
    public boolean canEnchant(ItemStack itemstack) {
        return wrapper.canEnchantItem(CraftItemStack.asBukkitCopy(itemstack));
    }
}
