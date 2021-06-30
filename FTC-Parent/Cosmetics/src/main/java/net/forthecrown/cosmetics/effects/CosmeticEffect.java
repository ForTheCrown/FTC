package net.forthecrown.cosmetics.effects;

import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public interface CosmeticEffect {

    boolean isOwnedBy(CrownUser user);
    boolean isCurrentActiveEffect(CrownUser user);

    String getEffectName();

    ItemStack getEffectItem();
    default void addGlow(ItemStack item) { item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1); }
    default void setItemOwned(ItemStack item) { item.setType(Material.ORANGE_DYE); }

    ClickableOption getClickableOption(CrownUser user);
}
