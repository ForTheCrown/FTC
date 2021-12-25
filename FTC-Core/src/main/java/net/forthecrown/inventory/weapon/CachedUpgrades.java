package net.forthecrown.inventory.weapon;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.utils.LoreBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

public class CachedUpgrades extends ObjectArrayList<WeaponUpgrade> {
    public static final Style
            FLUFF_STYLE = nonItalic(NamedTextColor.GRAY),
            LORE_STYLE  = nonItalic(NamedTextColor.GRAY);

    public boolean hasFluff() {
        if(isEmpty()) return false;
        for (WeaponUpgrade u: this) {
            if(u.loreFluff() != null) return true;
        }

        return false;
    }

    public void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag extraData) {
        forEach(upgrade -> upgrade.apply(sword, item, meta, extraData));
    }

    public void addFluff(LoreBuilder builder) {
        for (WeaponUpgrade u: this) {
            Component[] fluff = u.loreFluff();
            if(fluff == null) continue;

            for (Component c: fluff) {
                builder.add(
                        c.style(FLUFF_STYLE)
                );
            }
        }
    }

    public void addLore(LoreBuilder builder) {
        if(isEmpty()) return;
        builder.add(Component.empty());

        // single
        if(size < 2) {
            builder.add(
                    Component.text("Next upgrade: ")
                            .style(LORE_STYLE)
                            .append(get(0).loreDisplay())
            );
            return;
        }

        // multiple upgrades
        builder.add(Component.text("Next upgrades: ").style(nonItalic(NamedTextColor.GRAY)));

        for (WeaponUpgrade u: this) {
            builder.add(
                    Component.text("• ")
                            .style(LORE_STYLE)
                            .append(u.loreDisplay())
            );
        }
    }
}
