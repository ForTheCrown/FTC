package net.forthecrown.dungeons.enchantments;

import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

class EnchantHandle extends Enchantment {
    private final FtcEnchant wrapper;

    protected EnchantHandle(FtcEnchant wrapper, EnchantmentCategory type, EquipmentSlot... slotTypes) {
        super(Rarity.VERY_RARE, type, slotTypes);
        this.wrapper = wrapper;
    }

    @Override
    public Component getFullname(int level) {
        return PaperAdventure.asVanilla(wrapper.displayName(level));
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

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        wrapper.onAttack(
                user.getBukkitLivingEntity(),
                target.getBukkitEntity(),
                level
        );
    }

    @Override
    public void doPostHurt(LivingEntity user, Entity attacker, int level) {
        wrapper.onHurt(
                user.getBukkitLivingEntity(),
                attacker.getBukkitEntity(),
                level
        );
    }
}