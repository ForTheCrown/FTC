package net.forthecrown.inventory;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

import static net.forthecrown.inventory.weapon.RoyalSword.BORDER;
import static net.forthecrown.text.Text.nonItalic;

public class RoyalCrown extends ExtendedItem {

    public static final String
            TAG_RANK = "rank",
            TAG_QUEEN = "queen";

    @Getter @Setter
    private boolean queen;

    @Getter @Setter
    private int rank;

    public RoyalCrown(ExtendedItemType type, CompoundTag tag) {
        super(type, tag);

        this.queen = tag.getBoolean(TAG_QUEEN);
        this.rank = tag.getInt(TAG_RANK);
    }

    public RoyalCrown(ExtendedItemType type, UUID owner) {
        super(type, owner);

        rank = 1;
        queen = false;
    }

    public void upgrade(ItemStack item) {
        rank++;
        item.editMeta(this::applyRank);

        update(item);
    }

    @Override
    protected void onUpdate(ItemStack item, ItemMeta meta) {}

    @Override
    protected void writeLore(TextWriter writer) {
        writer.formattedLine("Rank {0, number, -roman}", NamedTextColor.DARK_GRAY, getRank());

        writer.line(BORDER);
        writer.line(Component.text("Only the worthy shall wear this", nonItalic(NamedTextColor.GOLD)));
        writer.line(Component.text("symbol of strength and power.", nonItalic(NamedTextColor.GOLD)));
        writer.line(BORDER);

        if (hasPlayerOwner()) {
            writer.formattedLine(
                    "Crafted for {0} {1, user}",
                    NamedTextColor.DARK_GRAY,

                    queen ? "Queen" : "King",
                    getOwner()
            );
        }
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt(TAG_RANK, rank);
        tag.putBoolean(TAG_QUEEN, queen);
    }

    public void applyRank(ItemMeta meta) {
        int rank = getRank();
        int enchantRank = rank;
        int extraHearts = rank * 2;
        double speedBoost = ((double) rank) / 20D;

        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);

        if (enchantRank > 0) {
            meta.addEnchant(Enchantment.PROTECTION_PROJECTILE, enchantRank, true);
            meta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, enchantRank, true);
            meta.addEnchant(Enchantment.PROTECTION_FIRE, enchantRank, true);
        }

        if (extraHearts > 0) {
            meta.removeAttributeModifier(Attribute.GENERIC_MAX_HEALTH);

            meta.addAttributeModifier(
                    Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(
                            UUID.randomUUID(), "generic.max_health", extraHearts,
                            AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD
                    )
            );
        }

        if (speedBoost > 0) {
            meta.removeAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED);

            meta.addAttributeModifier(
                    Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(
                            UUID.randomUUID(), "generic.movement_speed", speedBoost,
                            AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HEAD
                    )
            );
        }
    }
}