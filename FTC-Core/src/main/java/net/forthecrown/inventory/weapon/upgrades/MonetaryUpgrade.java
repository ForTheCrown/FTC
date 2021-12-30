package net.forthecrown.inventory.weapon.upgrades;

import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public record MonetaryUpgrade(int amount, Type type) implements WeaponUpgrade {
    @Override
    public void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag extraData) {
        CrownUser user = sword.getOwnerUser();

        type.add(user, amount);

        user.sendMessage(
                Component.text("Got ")
                        .color(NamedTextColor.GRAY)
                        .append(type.format(amount)
                                .color(NamedTextColor.YELLOW)
                        )
        );
    }

    @Override
    public Component loreDisplay() {
        return type.format(amount);
    }

    public enum Type {
        GEMS {
            @Override
            public void add(CrownUser user, int amount) {
                user.addGems(amount);
            }

            @Override
            public Component format(int amount) {
                return FtcFormatter.gems(amount);
            }
        },
        RHINES {
            @Override
            public void add(CrownUser user, int amount) {
                user.addBalance(amount);
            }

            @Override
            public Component format(int amount) {
                return FtcFormatter.rhines(amount);
            }
        };

        public abstract void add(CrownUser user, int amount);
        public abstract Component format(int amount);
    }
}
