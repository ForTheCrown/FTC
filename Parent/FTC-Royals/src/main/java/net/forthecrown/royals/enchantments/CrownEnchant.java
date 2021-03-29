package net.forthecrown.royals.enchantments;

import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

//Used for properly identifying things
public abstract class CrownEnchant extends Enchantment {
    public CrownEnchant(@NotNull NamespacedKey key) {
        super(key);
    }

    public static void addCrownEnchant(ItemStack itemStack, CrownEnchant enchant, int level){
        itemStack.addUnsafeEnchantment(enchant, level);
        List<Component> lore = new ArrayList<>();
        lore.add(enchant.displayName(level));
        if(itemStack.lore() != null) lore.addAll(itemStack.lore());
        itemStack.lore(lore);
    }

    @Override
    public @NotNull Component displayName(int level) {
        return ComponentUtils.convertString(getName() + (level <= 1 ? "" : (" " + CrownUtils.arabicToRoman(level)))).color(NamedTextColor.GRAY);
    }
}
