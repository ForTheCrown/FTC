package net.forthecrown.inventory.crown;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public interface CrownRank {
    void apply(ItemStack item, RoyalCrown crown, ItemMeta meta);
    int applyAtRank();

    static CrownRank simple(int rank, int enchantLevel, int extraHearts, double speedBoost) {
        return new SimpleCrownRank(rank, enchantLevel, extraHearts, speedBoost);
    }

    static CrownRank simple(int rank) {
        double speed = ((double) rank) / 20D;

        return simple(rank, rank, rank * 2, speed);
    }

    class SimpleCrownRank implements CrownRank {
        private final int rank, enchantRank, extraHearts;
        private final double speedBoost;

        public SimpleCrownRank(int rank, int enchantRank, int extraHearts, double speedBoost) {
            this.rank = rank;
            this.enchantRank = enchantRank;
            this.extraHearts = extraHearts * 2;
            this.speedBoost = speedBoost;
        }

        @Override
        public void apply(ItemStack item, RoyalCrown crown, ItemMeta meta) {
            if(enchantRank > 0) {
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, enchantRank, true);
                meta.addEnchant(Enchantment.PROTECTION_PROJECTILE, enchantRank, true);
                meta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, enchantRank, true);
                meta.addEnchant(Enchantment.PROTECTION_FALL, enchantRank, true);
                meta.addEnchant(Enchantment.PROTECTION_FIRE, enchantRank, true);
            }

            if(extraHearts > 0) {
                meta.removeAttributeModifier(Attribute.GENERIC_MAX_HEALTH);

                meta.addAttributeModifier(
                        Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(
                                UUID.randomUUID(), "generic.max_health", extraHearts,
                                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD
                        )
                );
            }

            if(speedBoost > 0) {
                meta.removeAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED);

                meta.addAttributeModifier(
                        Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(
                                UUID.randomUUID(), "generic.movement_speed", speedBoost,
                                AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HEAD
                        )
                );
            }
        }

        @Override
        public int applyAtRank() {
            return rank;
        }
    }
}
