package net.forthecrown.cosmetics.inventories.effects;

import net.forthecrown.core.user.CrownUser;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public interface CosmeticEffect {

    boolean isOwnedBy(CrownUser user);
    boolean isCurrentActiveEffect(CrownUser user);

    String getEffectName();

    ItemStack getEffectItem();
    default void addGlow(ItemStack item) { item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1); }
}
